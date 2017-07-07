package com.elminster.easy.rpc.client.processor;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.exception.RpcException;

public interface RpcClientProcessor {

  /**
   * Remote Method Call.
   * 
   * @param rpcCall
   *          the rpcCall
   * @return result
   * @throws RpcException
   *           on error
   */
  public Object invokeService(RpcCall rpcCall) throws Throwable;
}
