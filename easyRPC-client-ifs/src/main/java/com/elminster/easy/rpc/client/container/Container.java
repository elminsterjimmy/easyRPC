package com.elminster.easy.rpc.client.container;

import com.elminster.easy.rpc.client.connection.Connection;
import com.elminster.easy.rpc.exception.ConnectionException;

/**
 * The Client Container.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface Container {

  /**
   * Connect to the server.
   * 
   * @return the connection.
   * @throws ConnectionException
   *           on connection error
   */
  public Connection connect() throws ConnectionException;

  /**
   * Disconnect to the server.
   */
  public void disconnect();

  /**
   * Is container connected to the server?
   * 
   * @return if the container connected to the server
   */
  public boolean isConnected();
}
