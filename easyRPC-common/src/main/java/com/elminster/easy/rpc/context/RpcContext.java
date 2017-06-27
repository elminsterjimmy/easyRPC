package com.elminster.easy.rpc.context;

public interface RpcContext {
  
  public String getServerContainerClassName();
  public String getServerListenerClassName();
  public String getSocketFactoryClassName();
  public int getClientTimeout();
  public boolean getClientTcpNoDelay();
}
