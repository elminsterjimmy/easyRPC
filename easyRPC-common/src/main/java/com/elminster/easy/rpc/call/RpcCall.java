package com.elminster.easy.rpc.call;

import com.elminster.easy.rpc.context.InvokeContext;
import com.elminster.easy.rpc.request.RpcRequest;

/**
 * The RPC Call Interface.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcCall {
  
  public InvokeContext getContext();
  public void setContext(InvokeContext context);
  
  public ReturnResult getResult();
  public void setResult(ReturnResult result);
  public RpcRequest getRequest();
  
  
  public String getRequestId();
  public String getServiceName();
  public String getMethodName();
  public Object[] getArgs();
  public boolean isAsyncCall();
  public boolean isVoidReturn();
  
  public void setStatus(Status status);
  public Status getStatus();
  
  public Long getInvokeStartAt();
  public void setInvokeStartAt(long invokeStartAt);
  public Long getInvokeEndAt();
  public void setInvokeEndAt(long invokeEndAt);
  
  public Long getRpcCallStartAt();
  public void setRpcCallStartAt(long callStartAt);
  public Long getRpcCallEndAt();
  public void setRpcCallEndAt(long callEndAt);
  
  public long getInvokeDuration() throws CallNotFinishedException;
  public long getRpcCallDuration() throws CallNotFinishedException;
  
  public Long getTimeout();
  public int getPriority();
}
