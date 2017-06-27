package com.elminster.easy.rpc.server.connection;

import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.server.RpcServer;

abstract public class RpcConnectionImpl implements RpcConnection {
  
  protected final RpcServer rpcServer;
  
  public RpcConnectionImpl(RpcServer rpcServer) {
    this.rpcServer = rpcServer;
  }

  @Override
  public void run() {
    // TODO Auto-generated method stub
    // client check
    doRun();
  }
  
  abstract protected void doRun();

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    Thread.currentThread().interrupt();
  }

}
