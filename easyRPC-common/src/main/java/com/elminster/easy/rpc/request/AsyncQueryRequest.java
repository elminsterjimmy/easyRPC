package com.elminster.easy.rpc.request;

public interface AsyncQueryRequest extends Request {

  public String getId2Request();

  public long getTimeout();

  public byte getAction();

  public void queryDone();

  public boolean isQueryDone();

  public void requestGet();

  public boolean isRequestGet();

  public void cancel();

  public boolean isCancel();
}
