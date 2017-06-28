package com.elminster.easy.rpc.protocol;

public interface RequestProtocol extends Protocol {

  public String getMethodName();

  public void setMethodName(String methodName);

  public String getServiceName();

  public void setServiceName(String serviceName);

  public void setMethodArgs(Object... args);

  public Object[] getMethodArgs();
}