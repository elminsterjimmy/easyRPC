package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.RequestTypeProtocol;

/**
 * The request type protocol implementation.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RequestTypeProtocolImpl extends ProtocolImpl<Byte> implements RequestTypeProtocol {
  
  public RequestTypeProtocolImpl() {
  }

  @Override
  public void writeData(Byte type, RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    encodingFactory.writeInt8(type);
  }

  @Override
  public Byte readData(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    return encodingFactory.readInt8();
  }

}
