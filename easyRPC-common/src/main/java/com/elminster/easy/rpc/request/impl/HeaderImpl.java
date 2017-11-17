package com.elminster.easy.rpc.request.impl;

import com.elminster.easy.rpc.request.Header;

public class HeaderImpl implements Header {

  private String version;
  private byte requestType;

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public byte getRequestType() {
    return requestType;
  }

  public void setRequestType(byte requestType) {
    this.requestType = requestType;
  }
}
