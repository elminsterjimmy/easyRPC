package com.elminster.easy.rpc.server.processor;

/**
 * The RPC Server Worker.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcServiceProcessor {

  /**
   * Invoke service method.
   * 
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
  public ReturnResult invokeServiceMethod(String serviceName, String methodName, Object[] params) throws Throwable;
}
