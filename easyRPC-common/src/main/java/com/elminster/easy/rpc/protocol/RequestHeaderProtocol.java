package com.elminster.easy.rpc.protocol;

public interface RequestHeaderProtocol extends Protocol {
  
  public void setRequestId(String requestId);
  public String getRequestId();

  public void setEncoding(String encodingName);
  public String getEncoding();
}
