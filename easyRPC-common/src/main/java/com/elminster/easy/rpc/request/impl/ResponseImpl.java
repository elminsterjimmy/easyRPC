package com.elminster.easy.rpc.request.impl;

import com.elminster.easy.rpc.request.Response;

public class ResponseImpl implements Response {

  private String requestId;
  private String encoding;
  private boolean isVoidCall;
  private Object returnValue;
  private boolean isCancelled;
  private boolean isTimeout;

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public boolean isVoidCall() {
    return isVoidCall;
  }

  public void setVoidCall(boolean isVoidCall) {
    this.isVoidCall = isVoidCall;
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

  public boolean isTimedout() {
    return isTimeout;
  }

  public void setTimeout(boolean isTimeout) {
    this.isTimeout = isTimeout;
  }
}
