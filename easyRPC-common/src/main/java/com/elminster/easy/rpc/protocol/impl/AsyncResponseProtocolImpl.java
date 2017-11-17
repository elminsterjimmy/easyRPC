package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.AsyncResponseProtocol;
import com.elminster.easy.rpc.request.AsyncQueryResponse;
import com.elminster.easy.rpc.request.impl.AsyncQueryResponseImpl;

public class AsyncResponseProtocolImpl extends ProtocolImpl<AsyncQueryResponse> implements AsyncResponseProtocol {
  
  public AsyncResponseProtocolImpl() {
  }

  @Override
  public void writeData(AsyncQueryResponse aResponse, RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    encodingFactory.writeAsciiNullable(aResponse.getRequestId());
    encodingFactory.writeAsciiNullable(aResponse.getId2Request());
    encodingFactory.writeInt8(aResponse.getAction());
    encodingFactory.writeInt64(aResponse.getTimeout());
    encodingFactory.flush();
  }

  @Override
  public AsyncQueryResponse readData(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    AsyncQueryResponseImpl aResponse = new AsyncQueryResponseImpl();
    aResponse.setRequestId(encodingFactory.readAsciiNullable());
    aResponse.setId2Request(encodingFactory.readAsciiNullable());
    aResponse.setAction(encodingFactory.readInt8());
    aResponse.setTimeout(encodingFactory.readInt64());
    return aResponse;
  }
}
