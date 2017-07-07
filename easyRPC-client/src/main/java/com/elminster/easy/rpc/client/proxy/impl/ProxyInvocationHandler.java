package com.elminster.easy.rpc.client.proxy.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.impl.RpcCallImpl;
import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.idl.Async;
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
    
    RpcCall rpcCall = new RpcCallImpl(getRequestId(), isAsyncMethod(method), serviceName, method.getName(), args);
    return rpcClient.invokeService(rpcCall);
  }
  
  private boolean isAsyncMethod(Method method) {
    boolean isAsync = false;
    if (null != method.getAnnotation(Async.class)) {
      isAsync = true;
    } else {
      String methodName = method.getName();
      if (methodName.startsWith("async") || methodName.endsWith("Async")) {
        isAsync = true;
      }
    }
    return isAsync;
  }

  private String getRequestId() {
    return UUID.randomUUID().toString();
  }

  public Class<?> getRpcInterface() {
    return rpcInterface;
  }
}
