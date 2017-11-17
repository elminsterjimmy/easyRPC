package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.RequestHeaderProtocol;

public class RequestHeaderProtocolImpl extends ProtocolImpl<String> implements RequestHeaderProtocol {
  
  @Override
  void writeData(String encodingName, RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    encodingFactory.writeAsciiNullable(encodingName);
  }

  @Override
  String readData(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    return encodingFactory.readAsciiNullable();
  }
}
