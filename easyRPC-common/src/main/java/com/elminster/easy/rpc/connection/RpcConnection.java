package com.elminster.easy.rpc.connection;

import java.net.InetAddress;

public interface RpcConnection extends Runnable {

  public void close();
  
  public InetAddress getRemoteAddress();
  
  public int getRemotePort();
  
  public InetAddress getLocalAddress();
  
  public int getLocalPort();
}
