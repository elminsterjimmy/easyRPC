package com.elminster.easy.rpc.client.container;


import com.elminster.easy.rpc.client.connection.Connection;
import com.elminster.easy.rpc.exception.ConnectionException;

public interface Container {

  public Connection connect() throws ConnectionException;
  
  public void disconnect();
  
  public boolean isConnected();
}
