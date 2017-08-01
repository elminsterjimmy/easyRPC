package com.elminster.easy.rpc.server.container.listener.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.util.ExceptionUtil;
import com.elminster.easy.rpc.connection.SocketFactory;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.registery.SocketFactoryRegsitery;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.listener.ServerListener;
import com.elminster.easy.rpc.server.listener.RpcServerListenEvent;
import com.elminster.easy.rpc.server.listener.RpcServerListener;

/**
 * The Server Listener Base.
 * 
 * @author jinggu
 * @version 1.0
 */
abstract public class ServerListenerBase implements ServerListener {

  /** the logger. */
  private static final Logger logger = LoggerFactory.getLogger(ServerListenerBase.class);

  /** the RPC server. */
  protected final RpcServer rpcServer;
  /** the container. */
  protected final Container container;
  /** the Connection Endpoint. */
  protected final ConnectionEndpoint endpoint;
  /** the socket factory. */
  protected SocketFactory socketFactory;
  /** the server socket. */
  protected ServerSocket serverSocket;

  public ServerListenerBase(RpcServer rpcServer, Container container, ConnectionEndpoint endpoint) {
    this.rpcServer = rpcServer;
    this.container = container;
    this.endpoint = endpoint;
    try {
      socketFactory = SocketFactoryRegsitery.INSTANCE.getSocketFactory(rpcServer.getContext());
    } catch (ObjectInstantiationExcption e) {
      throw new RuntimeException("Cannot Instantiate socket factory!", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void listen() throws IOException {
    serverSocket = socketFactory.createServerSocket(endpoint.getPort(), endpoint.useSecureSocket());
    setupServerSocket(serverSocket);
    for (RpcServerListener l : rpcServer.getServerListeners()) {
      l.afterListened(new RpcServerListenEvent(endpoint));
    }
  }

  /**
   * Setup the server socket.
   * 
   * @param serverSocket
   *          the server socket
   */
  protected void setupServerSocket(ServerSocket serverSocket) {
    try {
      serverSocket.setSoTimeout(0);
    } catch (SocketException e) {
      e.printStackTrace();
      logger.warn("Failed to set server socket timeout to 0. Cause: " + ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * Setup the client socket.
   * 
   * @param socket
   *          the client socket
   */
  protected void setupClientSocket(Socket socket) {
    RpcContext context = rpcServer.getContext();
    try {
      if (null != context.getClientTimeout()) {
        socket.setSoTimeout(context.getClientTimeout());
      }
      if (null != context.getClientTcpNoDelay()) {
        socket.setTcpNoDelay(context.getClientTcpNoDelay());
      }
    } catch (IOException ioe) {
      logger.warn("Failed to set client socket timeout and tcp no delay flag. Cause: " + ExceptionUtil.getStackTrace(ioe));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws IOException {
    logger.info(String.format("RPC server stop at endpoint: %s.", endpoint.toString()));
    List<RpcServerListener> listeners = rpcServer.getServerListeners();
    for (RpcServerListener listener : listeners) {
      listener.beforeUnserve(new RpcServerListenEvent(endpoint));
    }
    if (null != serverSocket) {
      serverSocket.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void interrupt() {
  }
}
