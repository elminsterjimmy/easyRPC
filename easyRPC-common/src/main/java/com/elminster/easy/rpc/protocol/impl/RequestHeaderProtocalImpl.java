package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.RequestHeaderProtocol;

public class RequestHeaderProtocalImpl extends ProtocolImpl implements RequestHeaderProtocol {
  
  public RequestHeaderProtocalImpl(RpcEncodingFactory encodingFactory) {
    super(encodingFactory);
  }

  private String encodingName;

  @Override
  public void encode() throws IOException, RpcException {
    Assert.notNull(encodingName);
    encodingFactory.writeAsciiNullable(encodingName);
  }

  @Override
  public void decode() throws IOException, RpcException {
    this.encodingName = encodingFactory.readAsciiNullable();
  }

  @Override
  public void setEncoding(String encodingName) {
    this.encodingName = encodingName;
  }

  @Override
  public String getEncoding() {
    return encodingName;
  }
}
