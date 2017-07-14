package com.elminster.easy.rpc.server.container.listener.impl;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.impl.SimpleConnectionEndpoint;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.connection.impl.NioRpcConnection;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.impl.NioContainer;
import com.elminster.easy.rpc.server.listener.RpcServerAcceptEvent;
import com.elminster.easy.rpc.server.listener.RpcServerListener;

public class NioServerListenerImpl extends ServerListenerBase {

  private static final Logger logger = LoggerFactory.getLogger(NioServerListenerImpl.class);

  private volatile boolean stop;

  public NioServerListenerImpl(RpcServer rpcServer, Container container, ConnectionEndpoint endpoint) {
    super(rpcServer, container, endpoint);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcConnection accept() throws IOException {
    ServerSocketChannel serverChannel = serverSocket.getChannel();
    SocketChannel socketChannel = serverChannel.accept();
    logger.info(String.format("Get connection from socket [%s].", socketChannel));
    Socket socket = socketChannel.socket();
    for (RpcServerListener listener : rpcServer.getServerListeners()) {
      listener.onAccept(new RpcServerAcceptEvent(endpoint, SimpleConnectionEndpoint.createEndpoint(socket.getInetAddress().getHostAddress(), socket.getPort())));
    }
    setupClientSocket(socket);
    RpcConnection connection = new NioRpcConnection(rpcServer, container, socketChannel);
    return connection;
  }

  @Override
  public void listen() throws IOException {
    super.listen();
    try (Selector selector = Selector.open()) {
      ServerSocketChannel serverChannel = serverSocket.getChannel();
      serverChannel.register(selector, SelectionKey.OP_ACCEPT);
      
      stop = false;
      while (!stop) {
        try {
          if (selector.select(100) == 0) {
            continue;
          }
          Iterator<SelectionKey> selecionKeys = selector.selectedKeys().iterator();
          
          while (selecionKeys.hasNext()) {
            SelectionKey key = selecionKeys.next();
            selecionKeys.remove();
            
            if (!key.isValid()) {
              continue;
            }
            
            if (key.isAcceptable()) {
              RpcConnection connection = accept();
              container.addOpenConnection(connection);
              NioContainer nioContainer = (NioContainer) container;
              nioContainer.assign2Reader((NioRpcConnection) connection);
            }
          }
        } catch (IOException ioe) {
          ;
        }
      }
    }
  }
}