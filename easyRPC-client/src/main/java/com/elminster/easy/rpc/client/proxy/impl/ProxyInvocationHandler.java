package com.elminster.easy.rpc.client.proxy.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

import com.elminster.easy.rpc.call.impl.RpcCallImpl;
import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.data.Request;
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
    
    Request request = new Request();
    request.setAsync(isAsyncMethod(method));
    request.setMethodArgs(args);
    request.setMethodName(method.getName());
    request.setRequestId(getRequestId());
    request.setServiceName(serviceName);
    request.setVersion("1.0.0");
    
    RpcCallImpl rpcCall = new RpcCallImpl(request);
    return rpcClient.invokeService(rpcCall);
  }
  
  /**
   * Check if is an async method.
   * @param method the method
   * @return if is an async method
   */
  private com.elminster.easy.rpc.data.Async isAsyncMethod(Method method) {
    com.elminster.easy.rpc.data.Async isAsync = com.elminster.easy.rpc.data.Async.SYNC;
    if (null != method.getAnnotation(Async.class)) {
      isAsync = com.elminster.easy.rpc.data.Async.ASYNC;
    } else {
      String methodName = method.getName();
      if (methodName.startsWith("async") || methodName.endsWith("Async")) {
        isAsync = com.elminster.easy.rpc.data.Async.ASYNC;
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
