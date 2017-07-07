package com.elminster.easy.rpc.call.impl;

import com.elminster.easy.rpc.call.CallNotFinishedException;
import com.elminster.easy.rpc.call.ReturnResult;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.context.InvokeContext;

public class RpcCallImpl implements RpcCall {

  private InvokeContext context;
  private final String requestId;
  private final String serviceName;
  private final String methodName;
  private final Object[] args;
  private final boolean isAsync;
  private ReturnResult result;
  private Long invokeStartAt;
  private Long invokeEndAt;
  private Long callStartAt;
  private Long callEndAt;

  public RpcCallImpl(String requestId, boolean isAsync, String serviceName, String methodName, Object[] args) {
    this.requestId = requestId;
    this.isAsync = isAsync;
    this.serviceName = serviceName;
    this.methodName = methodName;
    this.args = args;
  }
  
  public RpcCallImpl(String requestId, boolean isAsync, String serviceName, String methodName, Object[] args, InvokeContext context) {
    this.requestId = requestId;
    this.isAsync = isAsync;
    this.serviceName = serviceName;
    this.methodName = methodName;
    this.args = args;
    this.context = context;
  }

  public ReturnResult getResult() {
    return result;
  }

  public void setResult(ReturnResult result) {
    this.result = result;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getMethodName() {
    return methodName;
  }

  public Object[] getArgs() {
    return args;
  }

  public InvokeContext getContext() {
    return context;
  }

  public Long getInvokeStartAt() {
    return invokeStartAt;
  }

  public void setInvokeStartAt(long invokeStartAt) {
    this.invokeStartAt = invokeStartAt;
  }

  public Long getInvokeEndAt() {
    return invokeEndAt;
  }

  public void setInvokeEndAt(long invokeEndAt) {
    this.invokeEndAt = invokeEndAt;
  }

  public Long getRpcCallStartAt() {
    return callStartAt;
  }

  public void setRpcCallStartAt(long callStartAt) {
    this.callStartAt = callStartAt;
  }

  public Long getRpcCallEndAt() {
    return callEndAt;
  }

  public void setRpcCallEndAt(long callEndAt) {
    this.callEndAt = callEndAt;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(String.format("RPC Call [%s] [%s@%s] with args %s on context [%s]", requestId, methodName, serviceName, generMethodArgs(args), context));
    if (null != result) {
      sb.append(String.format(" --> result [%s]", result.getReturnValue().toString()));
      try {
        long invokeDuration = this.getInvokeDuration();
        sb.append(String.format(" within [%d] ms (Invoke Duration)", invokeDuration));
      } catch (CallNotFinishedException e) {
        ;
      }
      try {
        long callDuration = this.getRpcCallDuration();
        sb.append(String.format(" [%d] ms (RPC Calling Duration)", callDuration));
      } catch (CallNotFinishedException e) {
        ;
      }
    }
    return sb.append(".").toString();
  }

  private static String generMethodArgs(Object[] args) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    boolean first = true;
    if (null != args) {
      for (Object arg : args) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append(arg);
      }
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public long getInvokeDuration() throws CallNotFinishedException {
    if (null == this.invokeEndAt || null == this.invokeStartAt) {
      throw new CallNotFinishedException();
    }
    return invokeEndAt.longValue() - invokeStartAt.longValue();
  }

  @Override
  public long getRpcCallDuration() throws CallNotFinishedException {
    if (null == this.callEndAt || null == this.callStartAt) {
      throw new CallNotFinishedException();
    }
    return callEndAt.longValue() - callStartAt.longValue();
  }

  @Override
  public boolean isAsyncCall() {
    return isAsync;
  }
  
  public void setContext(InvokeContext context) {
    this.context = context;
  }
}
