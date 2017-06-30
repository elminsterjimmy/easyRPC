package com.elminster.easy.rpc.context;

import java.net.InetAddress;

public interface InvokeContext {

  public InetAddress getInvokerHost();
  
  public int getInvokerPort();
  
  public String getInvokerVersion();
  
  public InetAddress getInvokeeHost();
  
  public int getInvokeePort();
  
  public String getInvokeeVersion();
}
