package com.elminster.easy.rpc.server.connection.impl;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.util.ExceptionUtil;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.data.Async;
import com.elminster.easy.rpc.data.Request;
import com.elminster.easy.rpc.data.Response;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.worker.impl.NioContainerReader;
import com.elminster.easy.rpc.server.container.worker.impl.NioContainerWriter;
import com.elminster.easy.rpc.server.container.worker.impl.WorkerJobId;
import com.elminster.easy.rpc.server.context.impl.InvokeeContextImpl;
import com.elminster.easy.rpc.server.context.impl.InvokeeContextImpl.InvokeeContextImplBuilder;

/**
 * NIO RPC Connection.
 * 
 * @author jinggu
 * @version 1.0
 */
public class NioRpcConnection extends RpcConnectionImpl {

  private static final AtomicInteger SERIAL = new AtomicInteger(WorkerJobId.NIO_CONNECTION.getJobId());

  private static final Logger logger = LoggerFactory.getLogger(NioRpcConnection.class);

  private final SocketChannel socketChannel;
  private final InetAddress localAddr;
  private final InetAddress remoteAddr;
  private final int localPort;
  private final int remotePort;
  private final Codec coreCodec;
  private final RpcEncodingFactory encodingFactory;
  private final InvokeeContextImpl context;
  private final BlockingQueue<Response> responses2write = new LinkedBlockingQueue<>();

  private NioContainerWriter writer;
  private NioContainerReader reader;

  private SelectionKey readerKey;

  private SelectionKey writerKey;

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
    encodingFactory = getEncodingFactory(coreCodec);
    InvokeeContextImplBuilder builder = new InvokeeContextImplBuilder();
    context = builder.withServerHost(localAddr).withClientHost(remoteAddr).withClientPort(remotePort).withServerPort(localPort).build();
    initializeBaseProtocols(encodingFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doRun() throws Exception {
    try {
      handleRequests(context);
    } catch (IOException ioe) {
      if (ioe instanceof EOFException) {
        logger.debug("EOF from client, close connection [{}].", this.getName());
      } else {
        logger.error(ExceptionUtil.getStackTrace(ioe));
      }
      this.close();
    }
  }

  private void handleRequests(InvokeeContextImpl invokeContext) throws IOException {
    try {
      logger.debug("NioRpcConnection#handleRequests()");
      Request request = requestProtocol.decode();
      if (null != request) {
        checkVersion(request.getVersion(), invokeContext);
        methodCall(request, invokeContext);
        if (Async.ASYNC == request.getAsync()) {
          Response response = new Response();
          response.setReqeustId(request.getRequestId());
          response.setVoid(false);
          response.setReturnValue("OK");
          addResponse2Write(response);
        }
      } else {
        logger.warn("decode request failed!");
      }
    } catch (RpcException rpce) {
      writeRpcException(rpce);
    }
  }

  @Override
  protected void writeResponse(Response response) throws IOException, RpcException {
    super.writeResponse(response);
    if (null != writerKey && writerKey.isValid()) {
      writerKey.interestOps(writerKey.interestOps() & ~SelectionKey.OP_WRITE);
    }
  }

  protected RpcCall toRpcCall(Request request, InvokeeContextImpl invokeContext) throws IOException, RpcException {
    RpcCall call = super.toRpcCall(request, invokeContext);
    NioRpcCall nioRpcCall = new NioRpcCallImpl(call, this, responseProtocol);
    return nioRpcCall;
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
  
  public boolean addFinishedCall(RpcCall call) {
    Response response = new Response();
    response.setReqeustId(call.getRequestId());
    response.setVoid(call.isVoidReturn());
    response.setReturnValue(call.getResult().getReturnValue());
    return addResponse2Write(response);
  }
  
  private boolean addResponse2Write(Response response) {
    boolean offered = this.responses2write.offer(response);
    if (offered) {
      nofityWrite();
    }
    return offered;
  }

  @Override
  public void close() {
    if (null != socketChannel) {
      if (socketChannel.isOpen()) {
        try {
          socketChannel.close();
        } catch (IOException e) {
          logger.warn(String.format("Failed to Close SocketChannel! Context [%s]", context), e);
        }
      }
    }
    coreCodec.close();
    container.removeOpenConnection(this);
  }

  @Override
  public String toString() {
    return String.format("NioRpcConnection [%X - %s]\n" + "[ Server=[ host:%s, port:%d ] | Client=[host:%s, port:%d] ]\n" + "[ Reader=[%s] ]", this.getId(), this.getName(),
        localAddr, localPort, remoteAddr, remotePort, reader);
  }
  
  /**
   * Try to read from the channel.
   * @throws IOException on error
   */
  public void read() throws Exception {
    this.doRun();
  }

  /**
   * Try to write to the channel.
   * 
   * @throws IOException
   *           on error
   */
  public void write() throws IOException {
    for (Response response : responses2write) {
      try {
        writeResponse(response);
      } catch (RpcException e) {
        logger.warn(e.getMessage());
      }
    }
  }

  /**
   * Register the writer.
   * 
   * @param writer
   *          the writer
   * @throws ClosedChannelException
   *           on error
   */
  public void registerWriter(NioContainerWriter writer) throws ClosedChannelException {
    this.writer = writer;
    this.writerKey = writer.registerChannel(this.socketChannel);
  }

  /**
   * Register the reader.
   * 
   * @param reader
   *          the reader
   * @throws ClosedChannelException
   *           on error
   */
  public void registerReader(NioContainerReader reader) throws ClosedChannelException {
    this.reader = reader;
    this.readerKey = reader.registerChannel(this.socketChannel);
  }

  private void nofityWrite() {
    logger.debug("Notify Done.");
    if (null != writerKey && writerKey.isValid()) {
      writerKey.interestOps(SelectionKey.OP_WRITE);
      writer.getSelector().wakeup();
    }
  }
}