package com.elminster.easy.rpc.server.connection.impl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
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
 * The Socket RPC Connection.
 * 
 * @author jinggu
 * @version 1.0
 */
public class SocketRpcConnection extends RpcConnectionImpl {

  private static final Logger logger = LoggerFactory.getLogger(SocketRpcConnection.class);

  private static final AtomicInteger SERIAL = new AtomicInteger(WorkerJobId.BIO_CONNECTION.getJobId());
  private final Socket socket;

  private final InetAddress localAddr;
  private final InetAddress remoteAddr;
  private final int localPort;
  private final int remotePort;
  
  {
    SERIAL.getAndIncrement();
  }
  
  public SocketRpcConnection(RpcServer server, Container container, Socket socket) {
    super(server, container, SERIAL.get(), "Socket Rpc Connection - " + Integer.toHexString(SERIAL.get()));
    this.socket = socket;
    localAddr = socket.getLocalAddress();
    remoteAddr = socket.getInetAddress();
    localPort = socket.getLocalPort();
    remotePort = socket.getPort();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doRun() throws Exception {
    // init the core codec
    InvokeeContextImpl invokeContext = null;
    try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
      CoreCodec coreCodec = CoreCodecFactory.INSTANCE.getCoreCodec(in, out);

      InvokeeContextImplBuilder builder = new InvokeeContextImplBuilder();
      invokeContext = builder.withServerHost(localAddr).withClientHost(remoteAddr).withClientPort(remotePort).withServerPort(localPort).build();
      
      RpcEncodingFactory defaultEncodingFactory = getEncodingFactory("default", coreCodec);
      
      initialBaseProtocols(defaultEncodingFactory);
      shakehand(defaultEncodingFactory);
      checkVersion(defaultEncodingFactory, invokeContext);

      while (!Thread.currentThread().isInterrupted()) {
        methodCall(defaultEncodingFactory, invokeContext, coreCodec);
      }
    } catch (IOException e) {
      if (e instanceof EOFException) {
        logger.warn(String.format(Messages.CLIENT_DISCONNECTED.getMessage(), invokeContext));
      } else {
        throw e;
      }
    } catch (RpcException e) {
      logger.error(e.getMessage());
      throw e;
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

  @Override
  public String toString() {
    return String.format("SocketRpcConnection [ Server=[ host:%s, port:%d ] | Client=[host:%s, port:%d] ]", localAddr, localPort, remoteAddr, remotePort);
  }
}
