package com.elminster.easy.rpc.client.context.impl;

import com.elminster.easy.rpc.context.RpcContext;

public class RpcClientContext implements RpcContext {

  private String clientContainerClassName;
  private String socketFactoryClassName;
  private Integer clientTimeout;
  private Boolean clientTcpNoDelay;
  
  public String getClientContainerClassName() {
    return clientContainerClassName;
  }
  public void setClientContainerClassName(String clientContainerClassName) {
    this.clientContainerClassName = clientContainerClassName;
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
  public String getServerContainerClassName() {
    return null;
  }
  @Override
  public String getServerListenerClassName() {
    return null;
  }
  @Override
  public String getServiceProcessorClassName() {
    return null;
  }
}
