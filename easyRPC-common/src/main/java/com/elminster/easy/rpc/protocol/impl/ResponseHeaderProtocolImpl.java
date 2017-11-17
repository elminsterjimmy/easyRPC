package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.ResponseHeaderProtocol;

public class ResponseHeaderProtocolImpl extends ProtocolImpl<String> implements ResponseHeaderProtocol {
  
  @Override
  void writeData(String encodingName, RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    encodingFactory.writeAsciiNullable(encodingName);
  }

  @Override
  String readData(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    return encodingFactory.readAsciiNullable();
  }
}
