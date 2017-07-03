package com.elminster.easy.rpc.server.processor.impl;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.factory.ReflectFactory;
import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessorFactory;

public class RpcServiceProcessorFactoryImpl extends ReflectFactory<RpcServiceProcessor> implements RpcServiceProcessorFactory {
  
  public static final RpcServiceProcessorFactory INSTANCE = new RpcServiceProcessorFactoryImpl();
  
  private RpcServiceProcessorFactoryImpl() {}

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcServiceProcessor createServiceProcessor(RpcServer rpcServer) throws ObjectInstantiationExcption {
    RpcContext context = rpcServer.getContext();
    String className = context.getServiceProcessorClassName();
    Assert.notNull(className);
    Class<?>[] classes = { RpcServer.class };
    Object[] args = { rpcServer };
    return super.instantiateInstance(className, classes, args);
  }

}
