package com.elminster.easy.rpc.server.connection;

import java.nio.channels.SocketChannel;

import com.elminster.easy.rpc.server.RpcServer;

public class NioRpcConnection extends RpcConnectionImpl {
  
  private final SocketChannel socketChannel;

  public NioRpcConnection(RpcServer server, SocketChannel socketChannel) {
    super(server);
    this.socketChannel = socketChannel;
  }

  @Override
  protected void doRun() {
    // TODO Auto-generated method stub
    
  }

}
