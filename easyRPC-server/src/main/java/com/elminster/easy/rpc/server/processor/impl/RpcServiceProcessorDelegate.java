package com.elminster.easy.rpc.server.processor.impl;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;

public class RpcServiceProcessorDelegate implements RpcServiceProcessor {
  
  private final SyncRpcServiceProcessor syncProcessor;
  private final AsyncRpcServiceProcessor asyncProcessor;

  public RpcServiceProcessorDelegate(RpcServer rpcServer) {
    syncProcessor = new SyncRpcServiceProcessor(rpcServer);
    asyncProcessor = new AsyncRpcServiceProcessor(rpcServer);
  }

  @Override
  public void invoke(RpcCall call) throws RpcException {
    if (call.isAsyncCall()) {
      asyncProcessor.invoke(call);
    } else {
      syncProcessor.invoke(call);
    }
  }

  @Override
  public RpcCall getResult(RpcCall call, int timeout) {
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
}
