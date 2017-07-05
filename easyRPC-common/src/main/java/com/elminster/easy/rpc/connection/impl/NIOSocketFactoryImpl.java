package com.elminster.easy.rpc.connection.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;

import javax.net.SocketFactory;

import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.connection.NIOSocketFactory;
import com.elminster.easy.rpc.context.ConnectionEndpoint;

/**
 * NIO Socket Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public class NIOSocketFactoryImpl implements NIOSocketFactory {

  /**
   * {@inheritDoc}
   */
  @Override
  public ServerSocket createServerSocket(int port, boolean useSecure) throws IOException {
    if (useSecure) {
      throw new UnsupportedOperationException("NIO Socket Factory doesn't support secure server socket yet.");
    }
    ServerSocketChannel serverChannel = ServerSocketChannel.open();
    serverChannel.configureBlocking(false);
    ServerSocket serverSocket = serverChannel.socket();
    serverSocket.setReuseAddress(true);
    serverSocket.bind(new InetSocketAddress(port));
    return serverSocket;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createClientSocket(ConnectionEndpoint connectionEndpoint) throws IOException {
    vaildateEndpoint(connectionEndpoint);
//    SocketChannel clientChannel = SocketChannel.open();
//    clientChannel.configureBlocking(false);
//    Socket clientSocket = clientChannel.socket();
//    clientChannel.connect(new InetSocketAddress(connectionEndpoint.getHost(), connectionEndpoint.getPort()));
//    return clientSocket;
    return SocketFactory.getDefault().createSocket(connectionEndpoint.getHost(), connectionEndpoint.getPort());
  }

  /**
   * Validate the connection endpoint.
   * 
   * @param connectionEndpoint
   *          the connection endpoint
   */
  private void vaildateEndpoint(ConnectionEndpoint connectionEndpoint) {
    Assert.notNull(connectionEndpoint);
    Assert.notNull(connectionEndpoint.getHost());
    Assert.notNull(connectionEndpoint.getPort());
    Assert.notNull(connectionEndpoint.useSecureSocket());
    if (connectionEndpoint.useSecureSocket()) {
      throw new UnsupportedOperationException("NIO Socket Factory doesn't support secure socket yet.");
    }
  }
}
