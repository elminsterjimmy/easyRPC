package com.elminster.easy.rpc.server.processor.impl;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;

/**
 * The RPC service processor delegate.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcServiceProcessorDelegate implements RpcServiceProcessor {
  
  private final SyncRpcServiceProcessor syncProcessor;
  private final AsyncRpcServiceProcessor asyncProcessor;

  public RpcServiceProcessorDelegate(RpcServer rpcServer) {
    syncProcessor = new SyncRpcServiceProcessor(rpcServer);
    asyncProcessor = new AsyncRpcServiceProcessor(rpcServer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void invoke(RpcCall call) throws RpcException {
    if (call.isAsyncCall()) {
      asyncProcessor.invoke(call);
    } else {
      syncProcessor.invoke(call);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcCall getResult(RpcCall call, long timeout) {
    if (call.isAsyncCall()) {
      return asyncProcessor.getResult(call, timeout);
    } else {
      return syncProcessor.getResult(call, timeout);
    }
  }

  public void close() {
    syncProcessor.close();
    asyncProcessor.close();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean cancelRpcCall(RpcCall call) {
    if (call.isAsyncCall()) {
      return asyncProcessor.cancelRpcCall(call);
    } else {
      return syncProcessor.cancelRpcCall(call);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcCall getRpcCall(String requestId) {
    RpcCall rpcCall = asyncProcessor.getRpcCall(requestId);
    if (null == rpcCall) {
      rpcCall = syncProcessor.getRpcCall(requestId);
    }
    return rpcCall;
  }
}
