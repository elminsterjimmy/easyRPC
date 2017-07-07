package com.elminster.easy.rpc.protocol;

public interface RequestProtocol extends Protocol {
  
  public String getRequestId();
  
  public void setRequestId(String requestId);

  public String getMethodName();

  public void setMethodName(String methodName);

  public String getServiceName();

  public void setServiceName(String serviceName);

  public void setMethodArgs(Object... args);

  public Object[] getMethodArgs();
  
  public boolean isAsyncCall();
  
  public void setAsyncCall(boolean isAsyncCall);
}