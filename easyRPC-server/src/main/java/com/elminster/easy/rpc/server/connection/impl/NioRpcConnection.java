package com.elminster.easy.rpc.server.connection.impl;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.data.Request;
import com.elminster.easy.rpc.data.Response;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
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
  private final RpcEncodingFactory defaultEncodingFactory;
  private final InvokeeContextImpl context;


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
    defaultEncodingFactory = server.getEncodingFactory(coreCodec);
    InvokeeContextImplBuilder builder = new InvokeeContextImplBuilder();
    context = builder.withServerHost(localAddr).withClientHost(remoteAddr).withClientPort(remotePort).withServerPort(localPort).build();
    initializeBaseProtocols(defaultEncodingFactory);
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
        this.close();
      } else {
        logger.error(ioe.getMessage(), ioe);
        throw ioe;
      }
    } finally {
      this.close();
    }
  }

  private void handleRequests(InvokeeContextImpl invokeContext) throws IOException {
    try {
      Request request = requestProtocol.decode();
      checkVersion(request.getVersion(), invokeContext);
      methodCall(request, invokeContext);
    } catch (RpcException rpce) {
      writeRpcException(rpce);
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
  
  @Override
  public void close() {
    super.close();
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
  }

  @Override
  public String toString() {
    return String.format("NioRpcConnection [ Server=[ host:%s, port:%d ] | Client=[host:%s, port:%d] ]", localAddr, localPort, remoteAddr, remotePort);
  }

  public void write() throws IOException {
    List<RpcCall> results = container.getServiceProcessor().getProccedResults(this);
    for (RpcCall call : results) {
      Response response = new Response();
      response.setReqeustId(call.getRequestId());
      response.setVoid(call.isVoidReturn());
      response.setReturnValue(call.getResult().getReturnValue());
      try {
        writeResponse(response);
      } catch (RpcException e) {
        logger.warn(e.getMessage());
      }
    }
  }
}