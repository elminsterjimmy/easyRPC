package com.elminster.easy.rpc.client.container.impl;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.factory.ReflectFactory;
import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.client.container.Container;
import com.elminster.easy.rpc.client.container.ContainerFactory;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;

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
  public Container getContainer(RpcClient rpcClient, ConnectionEndpoint endpoint) throws ObjectInstantiationExcption {
    RpcContext rpcContext = rpcClient.getRpcContext();
    String clientContainerClassName = rpcContext.getClientContainerClassName();
    Assert.notNull(clientContainerClassName);
    Class<?>[] classes = { RpcClient.class, ConnectionEndpoint.class };
    Object[] args = { rpcClient, endpoint };
    return super.instantiateInstance(clientContainerClassName, classes, args);
  }
}
