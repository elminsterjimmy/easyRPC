package com.elminster.easy.rpc.server.connection.impl;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.thread.IJobMonitor;
import com.elminster.common.thread.Job;
import com.elminster.common.threadpool.ThreadPool;
import com.elminster.common.threadpool.ThreadPoolConfiguration;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.Status;
import com.elminster.easy.rpc.codec.CoreCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.exception.IoTimeoutException;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.exception.ZeroReadException;
import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol.Frame;
import com.elminster.easy.rpc.protocol.ResponseProtocol;
import com.elminster.easy.rpc.protocol.impl.ProtocolFactoryImpl;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.worker.impl.WorkerJobId;
import com.elminster.easy.rpc.server.context.impl.InvokeeContextImpl;
import com.elminster.easy.rpc.server.context.impl.InvokeeContextImpl.InvokeeContextImplBuilder;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;

/**
 * NIO RPC Connection.
 * 
 * @author jinggu
 * @version 1.0
 */
public class NioRpcConnection extends RpcConnectionImpl {

  private static final AtomicInteger SERIAL = new AtomicInteger(WorkerJobId.NIO_CONNECTION.getJobId());

  private static final Logger logger = LoggerFactory.getLogger(NioRpcConnection.class);

  private final ThreadPool threadPool = new ThreadPool(ThreadPoolConfiguration.INSTANCE);

  private final SocketChannel socketChannel;
  private final InetAddress localAddr;
  private final InetAddress remoteAddr;
  private final int localPort;
  private final int remotePort;
  private final CoreCodec coreCodec;
  private final RpcEncodingFactory defaultEncodingFactory;
  private final InvokeeContextImpl invokeContext;

  private final Semaphore readSemaphore;
  private SelectionKey key;

  {
    SERIAL.getAndIncrement();
  }

  public NioRpcConnection(RpcServer server, Container container, SocketChannel socketChannel) {
    super(server, container, SERIAL.get(), "Nio Rpc Connection - " + Integer.toHexString(SERIAL.get()));
    this.socketChannel = socketChannel;
    Socket socket = socketChannel.socket();
    localAddr = socket.getLocalAddress();
    remoteAddr = socket.getInetAddress();
    localPort = socket.getLocalPort();
    remotePort = socket.getPort();
    readSemaphore = new Semaphore(0);

    coreCodec = CoreCodecFactory.INSTANCE.getCoreCodec(socketChannel);
    defaultEncodingFactory = server.getDefaultEncodingFactory(coreCodec);
    InvokeeContextImplBuilder builder = new InvokeeContextImplBuilder();
    invokeContext = builder.withServerHost(localAddr).withClientHost(remoteAddr).withClientPort(remotePort).withServerPort(localPort).build();
    try {
      initializeBaseProtocols(defaultEncodingFactory);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doRun() throws Exception {
    confirmFrameProtocol.nextFrame(Frame.FRAME_OK.getFrame());
    int retry = 0;
    while (!Thread.currentThread().isInterrupted()) {
      try {
        if (readSemaphore.tryAcquire(30, TimeUnit.SECONDS)) { // wait for reading
          handleRequests(defaultEncodingFactory, invokeContext);
          key.interestOps(SelectionKey.OP_READ);
          retry = 0;
        } else {
          throw new IoTimeoutException("Acquire read semaphore timeout with 30 sec!");
        }
      } catch (IOException ioe) {
        if (ioe instanceof EOFException) { // FIXME why cannot read -1 from disconnected socket????
          logger.debug("EOF from client, close connection [{}].", this.getName());
          this.close();
        } else if (ioe instanceof ZeroReadException) {
//          logger.warn("{} in {}.", ioe.getMessage(), this.getName());
          key.interestOps(SelectionKey.OP_READ);
          if (++retry > 30) {
            String message = "Zero Read exceed retry threshold! Seems connection borken, [CLOSE] connection!";
            this.close();
            throw new IoTimeoutException(message);
          } else {
            continue;
          }
        } else if (ioe instanceof IoTimeoutException) {
          logger.warn("{} in {}.", ioe.getMessage(), this.getName());
          this.close();
        } else {
          logger.error(String.format(Messages.CLIENT_DISCONNECTED.getMessage(), invokeContext));
          this.close();
          throw ioe;
        }
      }
    }
  }

  private void handleRequests(RpcEncodingFactory defaultEncodingFactory, InvokeeContextImpl invokeContext) throws IOException {
    try {
      confirmFrameProtocol.decode();
    } catch (RpcException e) {
      ;
    }
    try {
      if (Frame.FRAME_HEARTBEAT.getFrame() == confirmFrameProtocol.getFrame()) {
        // do nothing just let the underlayer core codec reset the retries count.
        if (logger.isDebugEnabled()) {
          logger.debug("received heartbeat from [{}]", invokeContext);
        }
      }
      if (Frame.FRAME_SHAKEHAND.getFrame() == confirmFrameProtocol.getFrame()) {
        shakehand(defaultEncodingFactory);
      } else if (Frame.FRAME_VERSION.getFrame() == confirmFrameProtocol.getFrame()) {
        checkVersion(defaultEncodingFactory, invokeContext);
      }
      if (Frame.FRAME_HEADER.getFrame() == confirmFrameProtocol.getFrame()) {
        // handle method calls
        methodCall(defaultEncodingFactory, invokeContext);
      } else if (Frame.FRAME_ASYNC_REQUEST.getFrame() == confirmFrameProtocol.getFrame()) {
        // handle async request
        handleAsyncRequest();
      }
    } catch (RpcException rpce) {
      confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
      writeRpcException(defaultEncodingFactory, rpce);
    }
  }

  private void handleAsyncRequest() throws IOException, RpcException {
    RpcServiceProcessor processor = container.getServiceProcessor();
    asyncProtocol.decode();
    String requestId = asyncProtocol.getRequestId();
    if (asyncProtocol.isCancel()) {
      cancelAsyncRpcCall(processor, requestId);
    } else if (asyncProtocol.isQueryDone()) {
      boolean isDone = checkRpcCallIsDone(processor, requestId);
      asyncProtocol.setRequestId(requestId);
      asyncProtocol.setDone(isDone);
      asyncProtocol.setTimeout(0);
      asyncProtocol.encode();
    } else if (asyncProtocol.isGet()) {
      long timeout = asyncProtocol.getTimeout();
      RpcCall call = processor.getRpcCall(requestId);
      // let the notifier to do the work in another thread
      Notifier notifier = new Notifier(call, timeout, processor);
      threadPool.execute(notifier);
    }
  }

  static class Notifier extends Job {

    private final RpcCall rpcCall;
    private final long timeout;
    private final RpcServiceProcessor processor;

    public Notifier(RpcCall rpcCall, long timeout, RpcServiceProcessor processor) {
      super(WorkerJobId.NIO_NOTIFIER.getJobId(), String.format("RPC Call Notifier for RPC Call [%s] and timeout [%d]", rpcCall, timeout));
      this.rpcCall = rpcCall;
      this.timeout = timeout;
      this.processor = processor;
    }

    @Override
    protected JobStatus doWork(IJobMonitor monitor) throws Throwable {
      processor.getResult(rpcCall, timeout);
      return monitor.done();
    }

  }

  /**
   * Invoke a method call.
   * 
   * @param defaultEncodingFactory
   *          the default encoding factory
   * @param invokeContext
   *          the invokee context
   * @param coreCodec
   *          the core codec
   * @throws IOException
   *           on error
   */
  protected void methodCall(RpcEncodingFactory defaultEncodingFactory, InvokeeContextImpl invokeContext) throws IOException, RpcException {
    RpcEncodingFactory rpcEncodingFactory = handleRequestHeader(defaultEncodingFactory, invokeContext, defaultEncodingFactory.getCoreCodec());
    RpcCall rpcCall = handleRequest(rpcEncodingFactory, invokeContext);
    ResponseProtocol responseProtocol;
    try {
      responseProtocol = (ResponseProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ResponseProtocol.class, rpcEncodingFactory);
    } catch (ObjectInstantiationExcption e) {
      // unexpected error
      confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
      String message = "Cannot instantiate request protocols, and this should NOT happened!";
      writeException(defaultEncodingFactory, e, message);
      return; // start over
    }
    RpcServiceProcessor proccessor = container.getServiceProcessor();
    rpcCall.setStatus(Status.UNPROCCESSED);
    if (rpcCall.isAsyncCall()) {
      confirmFrameProtocol.nextFrame(Frame.FRAME_ASYNC_RESPONSE.getFrame());
      handleAsyncRpcCall(proccessor, rpcCall, defaultEncodingFactory, responseProtocol);
    } else {
      handleSyncRpcCall(proccessor, rpcCall, defaultEncodingFactory, responseProtocol);
    }
  }

  protected void handleSyncRpcCall(RpcServiceProcessor proccessor, RpcCall call, RpcEncodingFactory defaultEncodingFactory, ResponseProtocol responseProtocol)
      throws IOException, RpcException {
    NioRpcCall nioRpcCall = new NioRpcCallImpl(call, this, responseProtocol);
    proccessor.invoke(nioRpcCall);
  }

  protected void handleAsyncRpcCall(RpcServiceProcessor proccessor, RpcCall call, RpcEncodingFactory defaultEncodingFactory, ResponseProtocol responseProtocol)
      throws IOException, RpcException {
    NioRpcCall nioRpcCall = new NioRpcCallImpl(call, this, responseProtocol);
    proccessor.invoke(nioRpcCall);
  }

  public void writeResponse(NioRpcCall nioRpcCall) throws IOException {
    try {
      this.writeResult(nioRpcCall, defaultEncodingFactory, nioRpcCall.getResponseProtocol());
    } catch (IOException ioe) {
      if (ioe instanceof EOFException) {
        logger.debug("EOF from client, close connection [{}].", this.getName());
        this.close();
      } else if (ioe instanceof ZeroReadException) {
        logger.warn("{} in {}.", ioe.getMessage(), this.getName());
      } else {
        logger.error(String.format(Messages.CLIENT_DISCONNECTED.getMessage(), invokeContext));
        this.close();
        throw ioe;
      }
    }
  }

  @Override
  public InetAddress getRemoteAddress() {
    return remoteAddr;
  }

  @Override
  public int getRemotePort() {
    return remotePort;
  }

  @Override
  public InetAddress getLocalAddress() {
    return localAddr;
  }

  @Override
  public int getLocalPort() {
    return localPort;
  }

  public SocketChannel getSocketChannel() {
    return this.socketChannel;
  }

  @Override
  public void close() {
    super.close();
    if (null != socketChannel) {
      if (socketChannel.isOpen()) {
        try {
          socketChannel.close();
        } catch (IOException e) {
          logger.warn(String.format("Failed to Close SocketChannel! Context [%s]", invokeContext), e);
        }
      }
    }
    coreCodec.close();
  }

  public void shakehand() throws IOException, RpcException {
    super.shakehand(defaultEncodingFactory);
  }

  public void checkVersion() throws IOException, RpcException {
    super.checkVersion(defaultEncodingFactory, invokeContext);
  }

  @Override
  public String toString() {
    return String.format("NioRpcConnection [ Server=[ host:%s, port:%d ] | Client=[host:%s, port:%d] ]", localAddr, localPort, remoteAddr, remotePort);
  }

  public void fireReadable(SelectionKey key) {
    this.key = key;
    if (JobStatus.RUNNING == this.getJobStatus()) {
      key.interestOps(0);
      readSemaphore.release();
    } else if (JobStatus.CREATED == this.getJobStatus()) {
      key.interestOps(SelectionKey.OP_READ);
    }
  }
}