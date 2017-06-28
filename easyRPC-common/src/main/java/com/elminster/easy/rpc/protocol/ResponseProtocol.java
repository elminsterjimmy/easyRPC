package com.elminster.easy.rpc.protocol;

public interface ResponseProtocal extends Protocal {

  public void setVoid(boolean isVoid);
  
  public boolean isVoid();
  
  public void setReturnValue();
  
  public Object getReturnValue();

  public void setReturnValue(Object returnValue);
}
