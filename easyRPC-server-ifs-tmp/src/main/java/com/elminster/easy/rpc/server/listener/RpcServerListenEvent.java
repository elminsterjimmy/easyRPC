package com.elminster.easy.rpc.server.listener;

import com.elminster.easy.rpc.context.ConnectionEndpoint;

public class RpcServerListenEvent {

  private ConnectionEndpoint endpoint;
  
  public RpcServerListenEvent(ConnectionEndpoint endpoint) {
    this.endpoint = endpoint;
  }
  
  public String getHost() {
    return endpoint.getHost();
  }
  
  public int getPort() {
    return endpoint.getPort().intValue();
  }
  
  public boolean useSecure() {
    return endpoint.useSecureSocket().booleanValue();
  }
}
