package com.elminster.easy.rpc.protocol;

public interface ResponseProtocol extends Protocol {

  public void setVoid(boolean isVoid);
  
  public boolean isVoid();
  
  public Object getReturnValue();

  public void setReturnValue(Object returnValue);
}
