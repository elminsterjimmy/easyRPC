package com.elminster.easy.rpc.call.impl;

import com.elminster.easy.rpc.call.CallNotFinishedException;
import com.elminster.easy.rpc.call.ReturnResult;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.Status;
import com.elminster.easy.rpc.context.InvokeContext;
import com.elminster.easy.rpc.request.RpcRequest;

public class RpcCallImpl implements RpcCall {

  private static final int PRIORITY_MID = 50;
  private InvokeContext context;
  private final RpcRequest request;
  private final long timeout;
  private final int priority;
  private boolean isVoidReturn;
  private ReturnResult result;
  private Long invokeStartAt;
  private Long invokeEndAt;
  private Long callStartAt;
  private Long callEndAt;
  private Status status;
  
  public RpcCallImpl(RpcRequest request) {
    this(request, null);
  }
  
  public RpcCallImpl(RpcRequest request, InvokeContext context) {
    this(request, context, PRIORITY_MID, 0L);
  }
  
  public RpcCallImpl(RpcRequest request, InvokeContext context, int priority, long timeout) {
    this.request = request;
    this.context = context;
    this.status = Status.CREATED;
    this.priority = priority;
    this.timeout = timeout;
  }

  public ReturnResult getResult() {
    return result;
  }

  public void setResult(ReturnResult result) {
    this.result = result;
  }

  public String getRequestId() {
    return request.getRequestId();
  }

  public String getServiceName() {
    return request.getServiceName();
  }

  public String getMethodName() {
    return request.getMethodName();
  }

  public Object[] getArgs() {
    return request.getMethodArgs();
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
    StringBuilder sb = new StringBuilder(String.format("RPC Call [%s] on context [%s]", request, context));
    if (null != result) {
      sb.append(String.format(" --> result [%s %s]", result.getReturnType().getName(), String.valueOf(result.getReturnValue())));
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
    return request.isAsyncCall();
  }
  
  public void setContext(InvokeContext context) {
    this.context = context;
  }

  public boolean isVoidReturn() {
    return isVoidReturn;
  }

  public void setVoidReturn(boolean isVoidReturn) {
    this.isVoidReturn = isVoidReturn;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getRequestId() == null) ? 0 : getRequestId().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RpcCallImpl other = (RpcCallImpl) obj;
    if (getRequestId() == null) {
      if (other.getRequestId() != null)
        return false;
    } else if (!getRequestId().equals(other.getRequestId()))
      return false;
    return true;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  @Override
  public Long getTimeout() {
    return this.timeout;
  }

  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public RpcRequest getRequest() {
    return request;
  }
}
