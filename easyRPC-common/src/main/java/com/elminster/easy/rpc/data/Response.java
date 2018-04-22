package com.elminster.easy.rpc.data;

public class Response {

  private String reqeustId;
  private boolean isVoid;
  private boolean isCancelled;
  private Object returnValue;

  public String getReqeustId() {
    return reqeustId;
  }

  public void setReqeustId(String reqeustId) {
    this.reqeustId = reqeustId;
  }

  public boolean isVoid() {
    return isVoid;
  }

  public void setVoid(boolean isVoid) {
    this.isVoid = isVoid;
  }

  public Object getReturnValue() {
    return returnValue;
  }

  public void setReturnValue(Object returnValue) {
    this.returnValue = returnValue;
  }

  public boolean isCancelled() {
    return isCancelled;
  }

  public void setCancelled(boolean isCancelled) {
    this.isCancelled = isCancelled;
  }
}
