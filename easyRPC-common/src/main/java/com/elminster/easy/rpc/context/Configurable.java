package com.elminster.easy.rpc.context;

public interface Configurable {

  public void setContext(RpcContext context);
  
  public RpcContext getContext();
}
