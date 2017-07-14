package com.elminster.easy.rpc.server.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;
import com.elminster.easy.rpc.service.RpcService;

public class SyncRpcServiceProcessor extends RpcServiceProcessorBase implements RpcServiceProcessor {
  
  private static final Logger logger = LoggerFactory.getLogger(SyncRpcServiceProcessor.class);
  
  public SyncRpcServiceProcessor(RpcServer rpcServer) {
    super(rpcServer);
  }
  
  private RpcCall invokeServiceMethod(RpcCall rpcCall) throws RpcException {
    RpcService service = getRpcService(rpcCall);
    return invokeInternal(service, rpcCall);
  }

  @Override
  public void invoke(RpcCall call) throws RpcException {
    unproccessedRpcCalls.put(call.getRequestId(), call);
    call = invokeServiceMethod(call);
    putProcessedCall(call);
  }

  @Override
  public RpcCall getResult(RpcCall rpcCall, long timeout) {
    return processedRpcCalls.remove(rpcCall.getRequestId());
  }
}
