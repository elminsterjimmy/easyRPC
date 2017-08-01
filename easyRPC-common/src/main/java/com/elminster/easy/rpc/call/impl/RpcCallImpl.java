package com.elminster.easy.rpc.call.impl;

import com.elminster.easy.rpc.call.CallNotFinishedException;
import com.elminster.easy.rpc.call.ReturnResult;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.Status;
import com.elminster.easy.rpc.context.InvokeContext;

public class RpcCallImpl implements RpcCall {

  private static final int PRIORITY_MID = 50;
  private InvokeContext context;
  private final String requestId;
  private final String serviceName;
  private final String methodName;
  private final Object[] args;
  private final boolean isAsync;
  private final long timeout;
  private final int priority;
  private boolean isVoidReturn;
  private ReturnResult result;
  private Long invokeStartAt;
  private Long invokeEndAt;
  private Long callStartAt;
  private Long callEndAt;
  private Status status;

  public RpcCallImpl(String requestId, boolean isAsync, String serviceName, String methodName, Object[] args) {
    this(requestId, isAsync, serviceName, methodName, args, null);
  }
  
  public RpcCallImpl(String requestId, boolean isAsync, String serviceName, String methodName, Object[] args, InvokeContext context) {
    this(requestId, isAsync, serviceName, methodName, args, context, PRIORITY_MID, 0L);
  }
  
  public RpcCallImpl(String requestId, boolean isAsync, String serviceName, String methodName, Object[] args, InvokeContext context, int priority, long timeout) {
    this.requestId = requestId;
    this.isAsync = isAsync;
    this.serviceName = serviceName;
    this.methodName = methodName;
    this.args = args;
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
    result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
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
    if (requestId == null) {
      if (other.requestId != null)
        return false;
    } else if (!requestId.equals(other.requestId))
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
}
