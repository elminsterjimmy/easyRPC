package com.elminster.easy.rpc.client.proxy.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.service.Rpc;

/**
 * The procy invocation handler for Rpc interface.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ProxyInvocationHandler implements InvocationHandler {
  
  private final Class<?> rpcInterface;
  private final RpcClient rpcClient;
  private final String serviceName;
  
  public ProxyInvocationHandler(Class<?> rpcInterface, RpcClient rpcClient) {
    this.rpcInterface = rpcInterface;
    Rpc rpc = rpcInterface.getAnnotation(Rpc.class);
    if (null == rpc) {
      throw new IllegalArgumentException(String.format("Class [%s] is not a Rpc interface!", rpcInterface.toString()));
    }
    this.rpcClient = rpcClient;
    this.serviceName = getServiceName(rpc);
  }

  private String getServiceName(Rpc rpc) {
    String name = rpc.value();
    if (null == name) {
      String simpleName = rpcInterface.getSimpleName();
      return simpleName.substring(0, simpleName.length() - 2);
    }
    return name;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (!rpcClient.isConnected()) {
      rpcClient.connect();
    }
    return rpcClient.invokeService(serviceName, method.getName(), args);
  }
  
  public Class<?> getRpcInterface() {
    return rpcInterface;
  }
}
