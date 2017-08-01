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

  private String requestId;
  private String encodingName;

  @Override
  public void writeData(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    Assert.notNull(requestId);
    Assert.notNull(encodingName);
    encodingFactory.writeAsciiNullable(requestId);
    encodingFactory.writeAsciiNullable(encodingName);
  }

  @Override
  public void readData(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    this.requestId = encodingFactory.readAsciiNullable();
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

  @Override
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  @Override
  public String getRequestId() {
    return this.requestId;
  }
}
