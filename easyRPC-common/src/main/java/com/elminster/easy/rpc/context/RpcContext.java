package com.elminster.easy.rpc.context;

public interface RpcContext {
  
  /////// server side context
  public String getServerContainerClassName();
  public String getServerListenerClassName();
  public String getServiceProcessorClassName();
  
  /////// client side context
  public String getClientContainerClassName();
  
  /////// both side context
  public String getSocketFactoryClassName();
  public Integer getClientTimeout();
  public Boolean getClientTcpNoDelay();
}
