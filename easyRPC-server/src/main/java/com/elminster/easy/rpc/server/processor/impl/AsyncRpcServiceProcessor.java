package com.elminster.easy.rpc.server.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.Status;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;

/**
 * RPC async service processor.
 * 
 * @author jinggu
 * @version 1.0
 */
public class AsyncRpcServiceProcessor extends RpcServiceProcessorBase implements RpcServiceProcessor {

  private static final Logger logger = LoggerFactory.getLogger(AsyncRpcServiceProcessor.class);
  
  private volatile boolean cancel = false;

  public AsyncRpcServiceProcessor(RpcServer rpcServer) {
    super(rpcServer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void invoke(RpcCall rpcCall) throws RpcException {
    try {
      queueForProcessing.put(rpcCall);
      unproccessedRpcCalls.put(rpcCall.getRequestId(), rpcCall);
    } catch (InterruptedException e) {
      logger.error("Put Rpc call [{}] to processing queue is interrupted!", rpcCall);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcCall getResult(String requestId, long timeout) {
    RpcCall result = processedRpcCalls.remove(requestId);
    if (null == result) {
      if (logger.isDebugEnabled()) {
        logger.debug("wait for [" + timeout + "] ms.");
      }
      if (timeout <= 0) {
        while (!Thread.currentThread().isInterrupted() && !cancel && (null == (result = processedRpcCalls.remove(requestId)))) {
          try {
            Thread.sleep(200);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      } else if (timeout > 10) {
        try {
          Thread.sleep(timeout);
        } catch (InterruptedException e) {
          return null;
        }
        result = processedRpcCalls.remove(requestId);
        if (null == result) {
          timeoutRpcCall(requestId);
        }
      }
    }
    return result;
  }
  
  private boolean timeoutRpcCall(String requestId) {
    logger.debug(String.format("timeout RPC Call [%s].", requestId));
    RpcCall call = this.getRpcCall(requestId);
    boolean b = cancelRpcCall(requestId);
    if (b) {
      call.setStatus(Status.TIMED_OUT);
    }
    this.cancel = true;
    return b;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean cancelRpcCall(String requestId) {
    super.cancelRpcCall(requestId);
    this.cancel = true;
    return true;
  }
}
