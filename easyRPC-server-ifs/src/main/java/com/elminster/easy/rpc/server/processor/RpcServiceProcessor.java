package com.elminster.easy.rpc.server.processor;

import java.util.List;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.connection.RpcConnection;
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
  public RpcCall getResult(String requestId, long timeout);

  /**
   * Get a RPC call results that submitted by specified RpcConnection.
   * 
   * @return the result
   */
  public List<RpcCall> getProccedResults(RpcConnection conn);

  /**
   * Cancel a RPC call.
   * 
   * @param call
   *          the RPC call
   * @return cancelled or not
   */
  public boolean cancelRpcCall(String requestId);

  /**
   * Get a RPC call by RPC call request id.
   * 
   * @param requestId
   *          the RPC call request id
   * @return
   */
  public RpcCall getRpcCall(String requestId);
}
