package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.ResponseProtocol;
import com.elminster.easy.rpc.request.Response;
import com.elminster.easy.rpc.request.impl.ResponseImpl;

/**
 * The Response Protocol Implementation.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ResponseProtocolImpl extends ProtocolImpl<Response> implements ResponseProtocol {

  public ResponseProtocolImpl() {
  }

  /**
   * {@docRoot}
   */
  @Override
  public void writeData(Response response, RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    encodingFactory.writeAsciiNullable(response.getRequestId());
    encodingFactory.writeIsNotNull(response.isVoidCall());
    encodingFactory.writeObjectNullable(response.getReturnValue());
    encodingFactory.writeAsciiNullable(response.getEncoding());
    encodingFactory.writeBoolean(response.isCancelled());
    encodingFactory.writeBoolean(response.isTimedout());
  }

  /**
   * {@docRoot}
   */
  @Override
  public Response readData(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    ResponseImpl response = new ResponseImpl();
    response.setRequestId(encodingFactory.readAsciiNullable());
    response.setVoidCall(encodingFactory.readIsNotNull());
    response.setReturnValue(encodingFactory.readObjectNullable());
    response.setEncoding(encodingFactory.readAsciiNullable());
    response.setCancelled(encodingFactory.readBoolean());
    response.setTimeout(encodingFactory.readBoolean());
    return response;
  }

}
