package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.AsyncRequestProtocol;
import com.elminster.easy.rpc.request.AsyncQueryRequest;
import com.elminster.easy.rpc.request.impl.AsyncQueryRequestImpl;

public class AsyncRequestProtocolImpl extends ProtocolImpl<AsyncQueryRequest> implements AsyncRequestProtocol {
  
  public AsyncRequestProtocolImpl() {
  }

  @Override
  public void writeData(AsyncQueryRequest request, RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    encodingFactory.writeAsciiNullable(request.getRequestId());
    encodingFactory.writeAsciiNullable(request.getId2Request());
    encodingFactory.writeAsciiNullable(request.getEncoding());
    encodingFactory.writeInt8(request.getAction());
    encodingFactory.writeInt64(request.getTimeout());
    encodingFactory.flush();
  }

  @Override
  public AsyncQueryRequest readData(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    AsyncQueryRequestImpl aRequest = new AsyncQueryRequestImpl();
    aRequest.setRequestId(encodingFactory.readAsciiNullable());
    aRequest.setId2Request(encodingFactory.readAsciiNullable());
    aRequest.setEncoding(encodingFactory.readAsciiNullable());
    aRequest.setAction(encodingFactory.readInt8());
    aRequest.setTimeout(encodingFactory.readInt64());
    return aRequest;
  }
}
