package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.data.Response;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.CodecException;
import com.elminster.easy.rpc.protocol.ResponseProtocol;

public class ResponseProtocolImpl extends ProtocolImpl<Response> implements ResponseProtocol {

  public ResponseProtocolImpl(RpcEncodingFactory encodingFactory) {
    super(encodingFactory);
  }

  @Override
  public void writeData(RpcEncodingFactory encodingFactory, Response response) throws IOException, CodecException {
    encodingFactory.writeAsciiNullable(response.getReqeustId());
    encodingFactory.writeIsNotNull(response.isVoid());
    encodingFactory.writeObjectNullable(response.getReturnValue());
  }

  @Override
  public Response readData(RpcEncodingFactory encodingFactory) throws IOException, CodecException {
    Response response = new Response();
    response.setReqeustId(encodingFactory.readAsciiNullable());
    response.setVoid(encodingFactory.readIsNotNull());
    response.setReturnValue(encodingFactory.readObjectNullable());
    return response;
  }
}
