package com.elminster.easy.rpc.service;

import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.exception.ConnectionException;

public interface RpcServiceFactory<T> {

  public T popService(ConnectionEndpoint endpoint) throws ConnectionException;
  
  public void pushService(T service) throws ConnectionException;
  
  public void shutdown() throws InterruptedException;
}
