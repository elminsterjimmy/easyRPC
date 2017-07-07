package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.ResponseProtocol;

public class ResponseProtocolImpl extends ProtocolImpl implements ResponseProtocol {

  private String requestId;
  private boolean isVoid;
  private Object returnValue;
  private Long invokeStart;
  private Long invokeEnd;
  
  public ResponseProtocolImpl(RpcEncodingFactory encodingFactory) {
    super(encodingFactory);
  }

  @Override
  public void encode() throws IOException, RpcException {
    encodingFactory.writeAsciiNullable(requestId);
    encodingFactory.writeInt64Nullable(invokeStart);
    encodingFactory.writeInt64Nullable(invokeEnd);
    encodingFactory.writeIsNotNull(isVoid);
    encodingFactory.writeObjectNullable(returnValue);
  }

  @Override
  public void decode() throws IOException, RpcException {
    requestId = encodingFactory.readAsciiNullable();
    invokeStart = encodingFactory.readInt64Nullable();
    invokeEnd = encodingFactory.readInt64Nullable();
    isVoid = encodingFactory.readIsNotNull();
    returnValue = encodingFactory.readObjectNullable();
  }

  @Override
  public String getRequestId() {
    return requestId;
  }

  @Override
  public void setRequestId(String requestId) {
    this.requestId = requestId;
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

  public Long getInvokeStart() {
    return invokeStart;
  }

  public void setInvokeStart(Long invokeStart) {
    this.invokeStart = invokeStart;
  }

  public Long getInvokeEnd() {
    return invokeEnd;
  }

  public void setInvokeEnd(Long invokeEnd) {
    this.invokeEnd = invokeEnd;
  }

}
