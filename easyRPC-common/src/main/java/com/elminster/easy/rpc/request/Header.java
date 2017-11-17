package com.elminster.easy.rpc.request;

public interface Header {
  
  public static final byte SYNC_REQUEST = 0;
  public static final byte ASYNC_REQUEST = 1;

  public String getVersion();
  
  public byte getRequestType();
}
