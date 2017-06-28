package com.elminster.easy.rpc.server.processor;

import java.net.InetAddress;

public interface InvokeContext {

  public InetAddress getInvokerHost();
  
  public int getInvokerPort();
  
  public String getInvokerVersion();
  
  public InetAddress getServerHost();
  
  public int getServerPort();
  
  public String getServerVersion();
}
