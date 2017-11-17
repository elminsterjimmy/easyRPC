package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.ShakehandProtocol;
import com.elminster.easy.rpc.protocol.exception.UnknownClientException;

public class ShakehandProtocolImpl extends ProtocolImpl<Void> implements ShakehandProtocol {

  private static final long[] MAGIC_NUMBER = { 0x656c6d, 0x696e7374, 0x65722e6a, 0x696d6d79 };

  public ShakehandProtocolImpl() {
  }

  @Override
  public void writeData(Void v, RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    for (long l : MAGIC_NUMBER) {
      encodingFactory.writeInt64(l);
    }
  }

  @Override
  public Void readData(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    for (long l : MAGIC_NUMBER) {
      long lNum = encodingFactory.readInt64();
      if (lNum != l) {
        throw new UnknownClientException("Unknow Client!");
      }
    }
    return null;
  }
}
