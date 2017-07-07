package com.elminster.easy.rpc.client.container.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.client.connection.Connection;
import com.elminster.easy.rpc.client.container.Container;
import com.elminster.easy.rpc.client.container.connection.BioConnectionImpl;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.exception.ConnectionException;

public class BioContainerImpl implements Container {

  private static final Logger logger = LoggerFactory.getLogger(BioContainerImpl.class);

  private final RpcClient rpcClient;
  private final ConnectionEndpoint endpoint;
  
  private List<Connection> openConnections = new LinkedList<>();

  public BioContainerImpl(RpcClient rpcClient, ConnectionEndpoint endpoint) {
    this.rpcClient = rpcClient;
    this.endpoint = endpoint;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Connection connect() throws ConnectionException {
    Connection connection = new BioConnectionImpl(rpcClient, endpoint, this);
    connection.connect();
    openConnections.add(connection);
    return connection;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void disconnect() {
    Iterator<Connection> it = openConnections.iterator();
    while (it.hasNext()) {
      Connection conn = it.next();
      it.remove();
      try {
        conn.disconnect();
      } catch (IOException e) {
        logger.warn("Exception on disconnect from connection.", e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isConnected() {
    return !openConnections.isEmpty();
  }

  public void removeConnection(BioConnectionImpl bioConnectionImpl) {
    openConnections.remove(bioConnectionImpl);
  }
}
