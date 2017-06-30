package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.RequestProtocol;

public class RequestProtocolImpl extends ProtocolImpl implements RequestProtocol {
  
  private String methodName;
  private String serviceName;
  private Object[] args;

  public RequestProtocolImpl(RpcEncodingFactory encodingFactory) {
    super(encodingFactory);
  }

  @Override
  public void encode() throws IOException, RpcException {
    encodingFactory.writeAsciiNullable(serviceName);
    encodingFactory.writeAsciiNullable(methodName);
    encodingFactory.writeInt32(args.length);
    for (Object arg : args) {
      encodingFactory.writeObjectNullable(arg);
    }
  }

  @Override
  public void decode() throws IOException, RpcException {
    this.serviceName = encodingFactory.readAsciiNullable();
    this.methodName = encodingFactory.readAsciiNullable();
    int len = encodingFactory.readInt32();
    this.args = new Object[len];
    for (int i = 0; i < len; i++) {
      this.args[i] = encodingFactory.readObjectNullable();
    }
  }

  @Override
  public String getMethodName() {
    return methodName;
  }

  @Override
  public void setMethodName(String methodName) {
    Assert.notNull(methodName);
    this.methodName = methodName;
  }

  @Override
  public String getServiceName() {
    return serviceName;
  }

  @Override
  public void setServiceName(String serviceName) {
    Assert.notNull(serviceName);
    this.serviceName = serviceName;
  }

  @Override
  public void setMethodArgs(Object... args) {
    if (null == args) {
      args = new Object[0];
    }
    this.args = args;
  }

  @Override
  public Object[] getMethodArgs() {
    return this.args;
  }

}
