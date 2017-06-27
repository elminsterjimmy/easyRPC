package com.elminster.easy.rpc.server.container.listener.impl;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.connection.SocketRpcConnection;

public class BioServerListenerImpl extends ServerListenerBase {

  private static final Logger logger = LoggerFactory.getLogger(BioServerListenerImpl.class);

  public BioServerListenerImpl(RpcServer rpcServer, ConnectionEndpoint endpoint) {
    super(rpcServer, endpoint);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcConnection accept() throws IOException {
    Socket socket = serverSocket.accept();
    logger.info(String.format("Get connection from socket [%s].", socket));
    setupClientSocket(socket);
    RpcConnection connection = new SocketRpcConnection(rpcServer, socket);
    return connection;
  }

}
