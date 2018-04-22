package com.elminster.easy.rpc.server.service.impl;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.Status;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;
import com.elminster.easy.rpc.server.service.AsyncService;

public class AsyncServiceImpl implements AsyncService {
  
  private static final String VERSION = "1.0.0";
  
  private final RpcServiceProcessor proccessor;
  
  public AsyncServiceImpl(RpcServiceProcessor processor) {
    this.proccessor = processor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDone(String requestId) {
    RpcCall call = proccessor.getRpcCall(requestId);
    return Status.isDone(call.getStatus());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object get(String requestId, long timeout) {
    return proccessor.getResult(requestId, timeout).getResult().getReturnValue();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean cancel(String requestId) {
    return proccessor.cancelRpcCall(requestId);
  }

  @Override
  public String[] getServiceMethods() {
    return new String[] {"isDone", "get", "cancel"};
  }

  @Override
  public String getServiceName() {
    return SERVICE_NAME;
  }

  @Override
  public String getServiceVersion() {
    return VERSION;
  }
}
