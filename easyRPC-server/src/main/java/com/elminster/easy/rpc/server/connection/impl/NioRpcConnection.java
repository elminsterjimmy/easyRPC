package com.elminster.easy.rpc.server.connection.impl;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
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
    try {
      handleRequests(defaultEncodingFactory, invokeContext, coreCodec);
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

  private void handleRequests(RpcEncodingFactory defaultEncodingFactory, InvokeeContextImpl invokeContext, CoreCodec coreCodec) throws IOException {
    try {
      confirmFrameProtocol.decode();
    } catch (RpcException e) {
      ;
    }
    try {
      if (Frame.FRAME_SHAKEHAND.getFrame() == confirmFrameProtocol.getFrame()) {
        shakehand(defaultEncodingFactory);
      } else if (Frame.FRAME_VERSION.getFrame() == confirmFrameProtocol.getFrame()) {
        checkVersion(defaultEncodingFactory, invokeContext);
      }
      if (Frame.FRAME_HEADER.getFrame() == confirmFrameProtocol.getFrame()) {
        // handle method calls
        methodCall(defaultEncodingFactory, invokeContext, coreCodec);
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
  protected void methodCall(RpcEncodingFactory defaultEncodingFactory, InvokeeContextImpl invokeContext, CoreCodec coreCodec) throws IOException, RpcException {
    RpcEncodingFactory rpcEncodingFactory = handleRequestHeader(defaultEncodingFactory, invokeContext, coreCodec);
    RpcCall rpcCall = handleRequest(rpcEncodingFactory, invokeContext, coreCodec);
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
    if (null != socketChannel) {
      if (socketChannel.isOpen()) {
        try {
          socketChannel.close();
        } catch (IOException e) {
          logger.warn(String.format("Failed to Close SocketChannel! Context [%s]", invokeContext), e);
        }
      }
    }
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
}