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

  public void invoke(RpcCall call) throws RpcException;
  
  public RpcCall getResult(RpcCall call, long timeout);
  
  public boolean cancelRpcCall(RpcCall call);
  
  public RpcCall getRpcCall(String requestId);
}
