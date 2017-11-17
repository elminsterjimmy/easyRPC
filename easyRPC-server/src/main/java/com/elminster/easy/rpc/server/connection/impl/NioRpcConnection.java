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

import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryNotFoundException;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryRepository;
import com.elminster.easy.rpc.exception.IoTimeoutException;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.exception.ZeroReadException;
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
  private final Codec codec;
  private final RpcEncodingFactoryRepository repository;
  private final InvokeeContextImpl context;

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

    codec = CoreCodecFactory.INSTANCE.getCoreCodec(socketChannel);
    repository = new RpcEncodingFactoryRepository() {

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
    InvokeeContextImplBuilder builder = new InvokeeContextImplBuilder();
    context = builder.withServerHost(localAddr).withClientHost(remoteAddr).withClientPort(remotePort).withServerPort(localPort).build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doRun() throws Exception {
    int retry = 0;
    try {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          if (readSemaphore.tryAcquire(30, TimeUnit.SECONDS)) { // wait for reading
            handleRequests(repository, codec, context);
            key.interestOps(SelectionKey.OP_READ);
            retry = 0;
          } else {
            throw new IoTimeoutException("Acquire read semaphore timeout with 30 sec!");
          }
        } catch (IOException ioe) {
          if (ioe instanceof ZeroReadException) {
            key.interestOps(SelectionKey.OP_READ);
            if (++retry > 30) {
              String message = "Zero Read exceed retry threshold! Seems connection borken, [CLOSE] connection!";
              throw new IoTimeoutException(message);
            } else {
              continue;
            }
          } else {
            throw ioe;
          }
        }
      }
    } catch (IOException ioe) {
      if (ioe instanceof EOFException) {
        logger.warn("EOF from client, close connection [{}].", this.getName());
      } else if (ioe instanceof IoTimeoutException) {
        logger.warn("{} in {}.", ioe.getMessage(), this.getName());
        throw ioe;
      } else {
        logger.error(ioe.getMessage(), ioe);
        throw ioe;
      }
    } finally {
      this.close();
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
  
  public void writeRpcCallResult(NioRpcCall call) throws IOException {
    try {
      writeResult(call, repository, codec);
    } catch (RpcException rpce) {
      writeException(repository, codec, rpce, call.getRequest());
    }
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
    codec.close();
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