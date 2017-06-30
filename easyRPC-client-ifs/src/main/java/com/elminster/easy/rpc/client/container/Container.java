package com.elminster.easy.rpc.client.container;

import com.elminster.easy.rpc.client.container.exception.ContainerConnectionException;
import com.elminster.easy.rpc.client.processor.RpcClientProcessor;

public interface Container {

  public void connect() throws ContainerConnectionException;
  
  public void disconnect();
  
  public boolean isConnected();
  
  public RpcClientProcessor getProcessor();
}
