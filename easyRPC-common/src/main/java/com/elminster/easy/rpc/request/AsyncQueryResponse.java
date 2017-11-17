package com.elminster.easy.rpc.request;

public interface AsyncQueryResponse {

  public long getTimeout();
  public void setTimeout(long timeout);

  public byte getAction();
  public void setAction(byte action);
  
  public String getRequestId();
  public void setRequestId(String requestId);
  
  public String getId2Request();
  public void setId2Request(String id2Request);
  
  public boolean isDone();
  public void setDone(boolean isDone);
  
  public boolean isCancelled();
  public void setCancelled(boolean cancelled);
}
