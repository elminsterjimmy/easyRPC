package com.elminster.easy.rpc.server.listener;

import com.elminster.common.listener.Listener;

public interface RpcServerListener extends Listener {

  public void beforeServe(RpcServerListenEvent event);
  
  public void preProcess();
  
  public void postProcess();
  
  public void beforeClose(RpcServerListenEvent event);
}
