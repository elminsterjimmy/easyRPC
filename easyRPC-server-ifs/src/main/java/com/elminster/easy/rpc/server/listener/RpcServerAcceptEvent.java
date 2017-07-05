package com.elminster.easy.rpc.server.listener;

import com.elminster.easy.rpc.context.ConnectionEndpoint;

public class RpcServerAcceptEvent {

  private final ConnectionEndpoint serverEndpoint;
  private final ConnectionEndpoint clientEndpoint;
  
  public RpcServerAcceptEvent(ConnectionEndpoint serverEndpoint, ConnectionEndpoint clientEndpoint) {
    this.serverEndpoint = serverEndpoint;
    this.clientEndpoint = clientEndpoint;
  }
  
  public ConnectionEndpoint getServerEndpoint() {
    return serverEndpoint;
  }
  public ConnectionEndpoint getClientEndpoint() {
    return clientEndpoint;
  }
}
