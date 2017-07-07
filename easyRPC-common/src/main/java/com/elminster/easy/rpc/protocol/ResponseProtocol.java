package com.elminster.easy.rpc.protocol;

public interface ResponseProtocol extends Protocol {
  
  public void setRequestId(String requestId);
  
  public String getRequestId();

  public void setVoid(boolean isVoid);
  
  public boolean isVoid();
  
  public Object getReturnValue();

  public void setReturnValue(Object returnValue);
  
  public Long getInvokeStart();
  
  public void setInvokeStart(Long invokeStart);
  
  public Long getInvokeEnd();
  
  public void setInvokeEnd(Long invokeEnd);
}
