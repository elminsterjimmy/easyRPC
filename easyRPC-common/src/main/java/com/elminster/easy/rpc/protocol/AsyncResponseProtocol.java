package com.elminster.easy.rpc.protocol;

public interface AsyncResponseProtocol extends Protocol {

  public long getTimeout();
  public void setTimeout(long timeout);
  
  public String getRequestId();
  public void setRequestId(String requestId);
  
  public void queryIsDone();
  public boolean isQueryDone();
  
  public boolean isDone();
  public void setDone(boolean isDone);
  
  public void cancel();
  public boolean isCancel();
  
  public void setGet();
  public boolean isGet();
}
