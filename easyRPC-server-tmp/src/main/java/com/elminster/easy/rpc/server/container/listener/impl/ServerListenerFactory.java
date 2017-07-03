package com.elminster.easy.rpc.server.container.listener.impl;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.factory.ReflectFactory;
import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.listener.ServerListener;
import com.elminster.easy.rpc.server.listener.RpcServerListenerFactory;

/**
 * The Server Listener Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ServerListenerFactory extends ReflectFactory<ServerListener> implements RpcServerListenerFactory {

  public static ServerListenerFactory INSTANCE = new ServerListenerFactory();

  private ServerListenerFactory() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ServerListener getServerListener(RpcServer rpcServer, ConnectionEndpoint endpoint)
      throws ObjectInstantiationExcption {
    RpcContext context = rpcServer.getContext();
    String serverlistenerClassName = context.getServerListenerClassName();
    Assert.notNull(serverlistenerClassName);
    Class<?>[] classes = { RpcServer.class, ConnectionEndpoint.class };
    Object[] args = { rpcServer, endpoint };
    return super.instantiateInstance(serverlistenerClassName, classes, args);
  }
}
