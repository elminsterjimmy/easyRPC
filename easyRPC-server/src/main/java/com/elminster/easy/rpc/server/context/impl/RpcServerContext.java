package com.elminster.easy.rpc.server.context.impl;

import com.elminster.common.threadpool.ThreadPoolConfiguration;
import com.elminster.easy.rpc.connection.impl.StreamSocketFactoryImpl;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.server.container.impl.BioContainer;
import com.elminster.easy.rpc.server.container.listener.impl.BioServerListenerImpl;
import com.elminster.easy.rpc.server.processor.impl.SyncRpcServiceProcessor;

public class RpcServerContext implements RpcContext {

  private String serverContainerClassName;
  private String serverListenerClassName;
  private String serviceProcessorClassName;
  private String socketFactoryClassName;
  private Integer clientTimeout;
  private Boolean clientTcpNoDelay;
  private ThreadPoolConfiguration workerThreadPoolConfiguration;
  
  public String getServerContainerClassName() {
    return serverContainerClassName;
  }
  public void setServerContainerClassName(String serverContainerClassName) {
    this.serverContainerClassName = serverContainerClassName;
  }
  public String getServerListenerClassName() {
    return serverListenerClassName;
  }
  public void setServerListenerClassName(String serverListenerClassName) {
    this.serverListenerClassName = serverListenerClassName;
  }
  public String getServiceProcessorClassName() {
    return serviceProcessorClassName;
  }
  public void setServiceProcessorClassName(String serviceProcessorClassName) {
    this.serviceProcessorClassName = serviceProcessorClassName;
  }
  public String getSocketFactoryClassName() {
    return socketFactoryClassName;
  }
  public void setSocketFactoryClassName(String socketFactoryClassName) {
    this.socketFactoryClassName = socketFactoryClassName;
  }
  public Integer getClientTimeout() {
    return clientTimeout;
  }
  public void setClientTimeout(Integer clientTimeout) {
    this.clientTimeout = clientTimeout;
  }
  public Boolean getClientTcpNoDelay() {
    return clientTcpNoDelay;
  }
  public void setClientTcpNoDelay(Boolean clientTcpNoDelay) {
    this.clientTcpNoDelay = clientTcpNoDelay;
  }
  @Override
  public String getClientContainerClassName() {
    return null;
  }
  @Override
  public ThreadPoolConfiguration getWorkerThreadPoolConfiguration() {
    return workerThreadPoolConfiguration;
  }
  public void setWorkerThreadPoolConfiguration(ThreadPoolConfiguration workerThreadPoolConfiguration) {
    this.workerThreadPoolConfiguration = workerThreadPoolConfiguration;
  }
  
  public static RpcContext createBioServerContext() {
    RpcServerContext context = new RpcServerContext();
    context.setServerContainerClassName(BioContainer.class.getName());
    context.setServerListenerClassName(BioServerListenerImpl.class.getName());
    context.setServiceProcessorClassName(SyncRpcServiceProcessor.class.getName());
    context.setSocketFactoryClassName(StreamSocketFactoryImpl.class.getName());
    context.setWorkerThreadPoolConfiguration(new ThreadPoolConfiguration());
    return context;
  }
}
