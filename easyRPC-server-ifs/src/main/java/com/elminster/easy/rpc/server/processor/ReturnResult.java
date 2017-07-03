package com.elminster.easy.rpc.server.processor;

public interface ReturnResult {

  public Object getReturnValue();
  
  public Class<?> getReturnType();
}
