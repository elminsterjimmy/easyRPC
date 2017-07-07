package com.elminster.easy.rpc.server.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;

public class AsyncRpcServiceProcessor extends RpcServiceProcessorBase implements RpcServiceProcessor {

  private static final Logger logger = LoggerFactory.getLogger(AsyncRpcServiceProcessor.class);
  
  public AsyncRpcServiceProcessor(RpcServer rpcServer) {
    super(rpcServer);
  }

  @Override
  public void invoke(RpcCall rpcCall) throws RpcException {
    try {
      processingQueue.put(rpcCall);
    } catch (InterruptedException e) {
      logger.error("Put Rpc call [" + rpcCall + "] to processing queue is interrupted!");
    }
  }

  @Override
  public RpcCall getResult(RpcCall rpcCall, int timeout) {
    String requestId = rpcCall.getRequestId();
    RpcCall result = processedRpcCalls.get(requestId);
    if (null == result) {
      if (logger.isDebugEnabled()) {
        logger.debug("wait for [" + timeout + "] ms.");
      }
      try {
        Thread.sleep(timeout);
      } catch (InterruptedException e) {
        return null;
      }
      return processedRpcCalls.get(requestId);
    } else {
      return result;
    }
  }
}
