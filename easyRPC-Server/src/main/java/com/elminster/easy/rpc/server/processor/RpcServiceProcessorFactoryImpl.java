package com.elminster.easy.rpc.server.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.elminster.common.util.ReflectUtil;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.exception.ObjectInstantiationExcption;
import com.elminster.easy.rpc.server.RpcServer;

public class RpcServiceProcessorFactoryImpl implements RpcServiceProcessorFactory {
  
  public static final RpcServiceProcessorFactory INSTANCE = new RpcServiceProcessorFactoryImpl();
  
  private RpcServiceProcessorFactoryImpl() {}

  @SuppressWarnings("unchecked")
  @Override
  public RpcServiceProcessor createServiceProcessor(RpcServer rpcServer) throws ObjectInstantiationExcption {
    RpcContext context = rpcServer.getContext();
    String className = context.getServiceProcessorClassName();
    try {
      Class<? extends RpcServiceProcessor> clazz = (Class<? extends RpcServiceProcessor>) ReflectUtil.forName(className);
      Constructor<? extends RpcServiceProcessor> constructor = ReflectUtil.getConstructor(clazz, RpcServer.class);
      RpcServiceProcessor processor = constructor.newInstance(rpcServer);
      return processor;
    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new ObjectInstantiationExcption(e);
    }
  }

}
