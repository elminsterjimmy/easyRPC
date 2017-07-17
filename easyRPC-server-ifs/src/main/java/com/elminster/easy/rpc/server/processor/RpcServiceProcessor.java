package com.elminster.easy.rpc.server.processor;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.exception.RpcException;

/**
 * The RPC Server Worker.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcServiceProcessor {

  /**
   * invoke a RPC call.
   * 
   * @param call
   *          the RPC call
   * @throws RpcException
   *           on error
   */
  public void invoke(RpcCall call) throws RpcException;

  /**
   * Get a RPC call result within timeout.
   * 
   * @param call
   *          the RPC call
   * @param timeout
   *          the timeout in ms
   * @return the result
   */
  public RpcCall getResult(RpcCall call, long timeout);

  /**
   * Cancel a RPC call.
   * 
   * @param call
   *          the RPC call
   * @return cancelled or not
   */
  public boolean cancelRpcCall(RpcCall call);

  /**
   * Get a RPC call by RPC call request id.
   * 
   * @param requestId
   *          the RPC call request id
   * @return
   */
  public RpcCall getRpcCall(String requestId);
}
