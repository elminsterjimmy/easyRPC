package com.elminster.easy.rpc.protocol;

public interface RequestHeaderProtocol extends Protocol {

  public void setEncoding(String encodingName);

  public String getEncoding();
}
