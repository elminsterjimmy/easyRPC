package com.elminster.easy.rpc.server.listener;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.listener.ServerListener;

public interface RpcServerListenerFactory {

  public ServerListener getServerListener(RpcServer rpcServer, ConnectionEndpoint endpoint) throws ObjectInstantiationExcption;
}
