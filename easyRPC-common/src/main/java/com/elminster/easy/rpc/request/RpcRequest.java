package com.elminster.easy.rpc.request;

public interface RpcRequest extends Request {

  public String getServiceVersion();
  
  public boolean isAsyncCall();
  
  public boolean isVoidCall();
  
  public String getServiceName();
  
  public String getMethodName();
  
  public Object[] getMethodArgs();
}
