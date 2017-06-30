package com.elminster.easy.rpc.client.processor;

import com.elminster.easy.rpc.exception.RpcException;

public interface RpcClientProcessor {

  /**
   * Remote Method Call.
   * 
   * @param serviceName
   *          the service name
   * @param methodName
   *          the method name
   * @param args
   *          the args
   * @return result
   * @throws RpcException
   *           on error
   */
  public Object invokeService(String serviceName, String methodName, Object[] args) throws Throwable;
}
