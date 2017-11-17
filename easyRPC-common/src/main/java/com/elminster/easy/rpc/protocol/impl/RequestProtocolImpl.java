package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.RequestProtocol;
import com.elminster.easy.rpc.request.RpcRequest;
import com.elminster.easy.rpc.request.impl.RpcRequestImpl;

public class RequestProtocolImpl extends ProtocolImpl<RpcRequest> implements RequestProtocol {
  
  public RequestProtocolImpl() {
  }

  @Override
  public void writeData(RpcRequest request, RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    encodingFactory.writeAsciiNullable(request.getRequestId());
    encodingFactory.writeAsciiNullable(request.getServiceVersion());
    encodingFactory.writeAsciiNullable(request.getServiceName());
    encodingFactory.writeAsciiNullable(request.getMethodName());
    encodingFactory.writeBoolean(request.isAsyncCall());
    encodingFactory.writeBoolean(request.isVoidCall());
    Object[] args = request.getMethodArgs();
    int argLen = 0;
    if (null != args) {
      argLen = args.length;
    }
    encodingFactory.writeInt32(argLen);
    if (null != args) {
      for (Object arg : args) {
        encodingFactory.writeObjectNullable(arg); // could happen encoding problem
      }
    }
  }

  @Override
  public RpcRequest readData(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    RpcRequestImpl request = new RpcRequestImpl();
    request.setRequestId(encodingFactory.readAsciiNullable());
    request.setServiceVersion(encodingFactory.readAsciiNullable());
    request.setServiceName(encodingFactory.readAsciiNullable());
    request.setMethodName(encodingFactory.readAsciiNullable());
    request.setAsyncCall(encodingFactory.readBoolean());
    request.setVoidCall(encodingFactory.readBoolean());
    int len = encodingFactory.readInt32();
    Object[] args = new Object[len];
    for (int i = 0; i < len; i++) {
      args[i] = encodingFactory.readObjectNullable();
    }
    request.setMethodArgs(args);
    request.setEncoding(encodingFactory.getName());
    return request;
  }
}
