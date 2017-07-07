package com.elminster.easy.rpc.server.connection.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.CoreCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
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
    invokeContext = builder.withServerHost(localAddr)
        .withClientHost(remoteAddr)
        .withClientPort(remotePort)
        .withServerPort(localPort).build();
  }

  @Override
  protected void doRun() throws Exception {
    try {
      if (null == shakehandProtocol) {
        initialBaseProtocols(defaultEncodingFactory);
        shakehand(defaultEncodingFactory);
      } else if (null == versionProtocol) {
        checkVersion(defaultEncodingFactory, invokeContext);
      } else {
        // method calls
        methodCall(defaultEncodingFactory, invokeContext, coreCodec);
      }
    } catch (IOException ioe) {
      logger.error(String.format(Messages.CLIENT_DISCONNECTED.getMessage(), invokeContext));
      this.close();
      throw ioe;
    } catch (RpcException rpce) {
      logger.error(rpce.getMessage());
      this.clone();
      throw rpce;
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
  
  @Override
  public String toString() {
    return String.format("NioRpcConnection [ Server=[ host:%s, port:%d ] | Client=[host:%s, port:%d] ]", localAddr, localPort, remoteAddr, remotePort);
  }
}
