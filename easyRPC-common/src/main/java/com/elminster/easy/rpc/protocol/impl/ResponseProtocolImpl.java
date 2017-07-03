package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.ResponseProtocol;

public class ResponseProtocolImpl extends ProtocolImpl implements ResponseProtocol {

  private boolean isVoid;
  private Object returnValue;
  
  public ResponseProtocolImpl(RpcEncodingFactory encodingFactory) {
    super(encodingFactory);
  }

  @Override
  public void encode() throws IOException, RpcException {
    encodingFactory.writeIsNotNull(isVoid);
    encodingFactory.writeObjectNullable(returnValue);
  }

  @Override
  public void decode() throws IOException, RpcException {
    isVoid = encodingFactory.readIsNotNull();
    returnValue = encodingFactory.readObjectNullable();
  }

  @Override
  public void setVoid(boolean isVoid) {
    this.isVoid = isVoid;
  }

  @Override
  public boolean isVoid() {
    return this.isVoid;
  }

  @Override
  public Object getReturnValue() {
    return returnValue;
  }

  @Override
  public void setReturnValue(Object returnValue) {
    this.returnValue = returnValue;
  }

}
