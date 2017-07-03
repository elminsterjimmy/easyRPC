package com.elminster.easy.rpc.server.container.listener;

import java.io.IOException;

import com.elminster.easy.rpc.connection.RpcConnection;

public interface ServerListener {

  public void listen() throws IOException;
  
  public RpcConnection accept() throws IOException;
  
  public void close() throws IOException;
  
  public void interrupt();
  
}
