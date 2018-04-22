package com.elminster.easy.rpc.server.service;

import com.elminster.easy.rpc.service.RpcService;

public interface AsyncService extends RpcService {
  
  public String SERVICE_NAME = "ASYNC";

  /**
   * Check the RPC call is done or not.
   * 
   * @param requestId
   *          the RPC call request id
   * @return the RPC call is done or not
   */
  public boolean isDone(String requestId);
  
  public Object get(String requestId, long timeout);

  /**
   * Cancel the RPC call.
   * 
   * @param proccessor
   *          the processor
   * @param requestId
   *          the RPC call request id
   * @return the RPC call is cancelled or not
   */
  public boolean cancel(String requestId);
}
