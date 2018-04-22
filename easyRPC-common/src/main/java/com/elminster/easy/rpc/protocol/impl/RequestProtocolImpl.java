package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.data.Async;
import com.elminster.easy.rpc.data.Request;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.CodecException;
import com.elminster.easy.rpc.protocol.RequestProtocol;
import com.elminster.easy.rpc.protocol.exception.UnknownClientException;

/**
 * The request protocol implementation.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RequestProtocolImpl extends ProtocolImpl<Request> implements RequestProtocol {

  private static final long[] MAGIC_NUMBER = { 0x656c6d, 0x696e7374, 0x65722e6a, 0x696d6d79 };

  public RequestProtocolImpl(RpcEncodingFactory encodingFactory) {
    super(encodingFactory);
  }

  @Override
  Request readData(RpcEncodingFactory encodingFactory) throws IOException, CodecException {
    for (long l : MAGIC_NUMBER) {
      long lNum = encodingFactory.readInt64();
      if (lNum != l) {
        throw new UnknownClientException("Unknow Client!");
      }
    }
    Request request = new Request();
    request.setVersion(encodingFactory.readStringNullable());
    request.setRequestId(encodingFactory.readStringNullable());
    byte b = encodingFactory.readInt8();
    request.setAsync(Async.toAsync(b));
    request.setServiceName(encodingFactory.readStringNullable());
    request.setMethodName(encodingFactory.readStringNullable());
    int len = encodingFactory.readInt32();
    Object[] methodArgs = new Object[len];
    for (int i = 0; i < len; i++) {
      methodArgs[i] = encodingFactory.readObjectNullable();
    }
    request.setMethodArgs(methodArgs);
    return request;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  void writeData(RpcEncodingFactory encodingFactory, Request request) throws IOException, CodecException {
    for (long l : MAGIC_NUMBER) {
      encodingFactory.writeInt64(l);
    }
    encodingFactory.writeStringNullable(request.getVersion());
    encodingFactory.writeStringNullable(request.getRequestId());
    encodingFactory.writeInt8(request.getAsync().value());
    encodingFactory.writeStringNullable(request.getServiceName());
    encodingFactory.writeStringNullable(request.getMethodName());
    Object[] methodArgs = request.getMethodArgs();
    if (null == methodArgs) {
      methodArgs = new Object[0];
    }
    encodingFactory.writeInt32(methodArgs.length);
    for (Object arg : methodArgs) {
      encodingFactory.writeObjectNullable(arg); // could happen encoding problem
    }
  }
}
