package com.elminster.easy.rpc.server.listener;

import com.elminster.common.listener.Listener;

public interface RpcServerListener extends Listener {

  public void beforeServe(RpcServerListenEvent event);
  
  public void afterListened(RpcServerListenEvent event);
  
  public void preProcess(RpcProcessEvent event);
  
  public void postProcess(RpcProcessEvent event);
  
  public void beforeClose(RpcServerListenEvent event);
}
