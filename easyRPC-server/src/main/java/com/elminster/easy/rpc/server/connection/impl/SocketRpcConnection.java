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

import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryNotFoundException;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryRepository;
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
    InvokeeContextImpl context = null;
    Codec codec = null;
    try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
      codec = CoreCodecFactory.INSTANCE.getCoreCodec(in, out);
      
      InvokeeContextImplBuilder builder = new InvokeeContextImplBuilder();
      context = builder.withServerHost(localAddr).withClientHost(remoteAddr).withClientPort(remotePort).withServerPort(localPort).build();

      RpcEncodingFactoryRepository repository = new RpcEncodingFactoryRepository() {

        @Override
        public RpcEncodingFactory getRpcEncodingFactory(String encodingFactoryName, Codec codec) throws RpcEncodingFactoryNotFoundException {
          return getEncodingFactory(encodingFactoryName, codec);
        }

        @Override
        public void addRpcEncodingFactory(RpcEncodingFactory encodingFactory) {
        }

        @Override
        public void removeRpcEncodingFactory(String encodingFactoryName) {
        }
      };

      while (!Thread.currentThread().isInterrupted()) {
        handleRequests(repository, codec, context);
      }
    } catch (IOException e) {
      if (e instanceof EOFException) {
        logger.warn(String.format(Messages.CLIENT_DISCONNECTED.getMessage(), context));
      } else {
        logger.error(e.getMessage(), e);
        throw e;
      }
    } finally {
      if (null != codec) {
        codec.close();
      }
    }
  }

  @Override
  protected void cleanUp() {
    super.cleanUp();
    // remove
    container.removeOpenConnection(this);
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
