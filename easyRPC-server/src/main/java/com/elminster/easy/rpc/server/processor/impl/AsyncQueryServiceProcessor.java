package com.elminster.easy.rpc.server.processor.impl;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;
import com.elminster.easy.rpc.server.service.AsyncService;
import com.elminster.easy.rpc.server.service.impl.AsyncServiceImpl;
import com.elminster.easy.rpc.service.RpcService;

public class AsyncQueryServiceProcessor extends SyncRpcServiceProcessor implements RpcServiceProcessor {
  
  protected final AsyncService asyncService;

  public AsyncQueryServiceProcessor(RpcServer rpcServer, RpcServiceProcessor processor) {
    super(rpcServer);
    this.asyncService = new AsyncServiceImpl(processor);
  }
  
  protected RpcService getRpcService(RpcCall rpcCall) throws RpcException {
    return asyncService;
  }
}
