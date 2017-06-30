package com.elminster.easy.rpc.client.proxy.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.client.proxy.RpcProxy;

/**
 * The Dynamic Proxy for RPC interface.
 * 
 * @author jinggu
 * @version 1.0
 */
public class DynamicProxy implements RpcProxy {

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T makeProxy(Class<T> rpcInterface, RpcClient rpcClient) {
    InvocationHandler handler = new ProxyInvocationHandler(rpcInterface, rpcClient);
    return (T) Proxy.newProxyInstance(rpcInterface.getClassLoader(), new Class[] {rpcInterface}, handler);
  }

}
