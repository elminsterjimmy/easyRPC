package com.elminster.easy.rpc.context;

import com.elminster.common.threadpool.ThreadPoolConfiguration;

public interface RpcContext {
  
  /////// server side context
  public String getServerContainerClassName();
  public String getServiceProcessorClassName();
  public ThreadPoolConfiguration getWorkerThreadPoolConfiguration();
  public Integer getReaderWorkerCount();
  
  /////// client side context
  public String getClientContainerClassName();
  
  /////// both side context
  public String getSocketFactoryClassName();
  public Integer getClientTimeout();
  public Boolean getClientTcpNoDelay();
}
