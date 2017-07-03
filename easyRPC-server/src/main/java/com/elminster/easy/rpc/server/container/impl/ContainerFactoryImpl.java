package com.elminster.easy.rpc.server.container.impl;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.factory.ReflectFactory;
import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.ContainerFactory;

/**
 * The Container Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ContainerFactoryImpl extends ReflectFactory<Container> implements ContainerFactory {

  public static final ContainerFactory INSTANCE = new ContainerFactoryImpl();
  
  private ContainerFactoryImpl() {}
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Container getContainer(RpcServer rpcServer, ConnectionEndpoint endpoint) throws ObjectInstantiationExcption {
    RpcContext rpcContext = rpcServer.getContext();
    String serverContainerClassName = rpcContext.getServerContainerClassName();
    Assert.notNull(serverContainerClassName);
    Class<?>[] classes = { RpcServer.class, ConnectionEndpoint.class };
    Object[] args = { rpcServer, endpoint };
    return super.instantiateInstance(serverContainerClassName, classes, args);
  }
}
