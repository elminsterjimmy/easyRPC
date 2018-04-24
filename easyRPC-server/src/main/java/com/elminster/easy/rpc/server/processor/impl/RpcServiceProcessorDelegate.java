package com.elminster.easy.rpc.server.processor.impl;

import java.util.ArrayList;
import java.util.List;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.connection.impl.NioRpcCall;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;
import com.elminster.easy.rpc.server.service.AsyncService;

/**
 * The RPC service processor delegate.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcServiceProcessorDelegate implements RpcServiceProcessor {
  
  private final SyncRpcServiceProcessor syncProcessor;
  private final AsyncRpcServiceProcessor asyncProcessor;
  private final AsyncQueryServiceProcessor asyncQueryProcessor;

  public RpcServiceProcessorDelegate(RpcServer rpcServer) {
    syncProcessor = new SyncRpcServiceProcessor(rpcServer);
    asyncProcessor = new AsyncRpcServiceProcessor(rpcServer);
    asyncQueryProcessor = new AsyncQueryServiceProcessor(rpcServer, asyncProcessor);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void invoke(RpcCall call) throws RpcException {
    if (isAsyncQueryCall(call)) {
      asyncQueryProcessor.invoke(call);
    } else if (isAsyncCall(call)) {
      asyncProcessor.invoke(call);
    } else {
      syncProcessor.invoke(call);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcCall getResult(String requestId, long timeout) {
    RpcCall call = getRpcCall(requestId);
    if (isAsyncCall(call)) {
      return asyncProcessor.getResult(requestId, timeout);
    } else if (isAsyncQueryCall(call)) {
      return asyncQueryProcessor.getResult(requestId, timeout);
    } else {
      return syncProcessor.getResult(requestId, timeout);
    }
  }

  public void close() {
    asyncQueryProcessor.close();
    syncProcessor.close();
    asyncProcessor.close();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean cancelRpcCall(String requestId) {
    RpcCall call = getRpcCall(requestId);
    if (isAsyncQueryCall(call)) {
      return asyncQueryProcessor.cancelRpcCall(requestId);
    } else if (isAsyncCall(call)) {
      return asyncProcessor.cancelRpcCall(requestId);
    } else {
      return syncProcessor.cancelRpcCall(requestId);
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
  
  private boolean isAsyncCall(RpcCall call) {
    return call.isAsyncCall() || call instanceof NioRpcCall;
  }
  
  private boolean isAsyncQueryCall(RpcCall call) {
    return AsyncService.SERVICE_NAME.equals(call.getServiceName());
  }

  @Override
  public List<RpcCall> getProccedResults(RpcConnection conn) {
    List<RpcCall> rtn = new ArrayList<>();
    List<RpcCall> asyncList = asyncProcessor.getProccedResults(conn);
    rtn.addAll(asyncList);
    List<RpcCall> asyncQueryList = asyncQueryProcessor.getProccedResults(conn);
    rtn.addAll(asyncQueryList);
    return rtn;
  }
}
