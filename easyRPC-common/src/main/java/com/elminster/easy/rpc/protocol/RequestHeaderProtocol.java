package com.elminster.easy.rpc.protocol;

public interface RequestHeaderProtocal extends Protocal {

  public void setEncoding(String encodingName);

  public String getEncoding();
}
