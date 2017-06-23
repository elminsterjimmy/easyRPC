package com.elminster.easy.rpc.server.worker;

import com.elminster.easy.rpc.context.RpcContext;

/**
 * The RPC Server Worker.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcServerWorker {

  /**
   * Invoke service method.
   * 
   * @param clientCallContext
   *          the context
   * @param serviceName
   *          the service name
   * @param methodName
   *          the method name
   * @param params
   *          the params
   * @return the invoke result
   * @throws Throwable
   *           on error
   */
  public Object[] invokeServiceMethod(RpcContext clientCallContext, String serviceName, String methodName, Object[] params) throws Throwable;
}
