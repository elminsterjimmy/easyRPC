package com.elminster.easy.rpc.server.connection;

import java.net.InetAddress;
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

  @Override
  public InetAddress getRemoteAddress() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getRemotePort() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public InetAddress getLocalAddress() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getLocalPort() {
    // TODO Auto-generated method stub
    return 0;
  }

}
