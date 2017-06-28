package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.protocol.Protocol;

abstract public class ProtocolImpl implements Protocol {

  protected final RpcEncodingFactory encodingFactory;
  
  public ProtocolImpl(RpcEncodingFactory encodingFactory) {
    this.encodingFactory = encodingFactory;
  }

  @Override
  public void fail() throws IOException {
    encodingFactory.writeIsNotNull(false);
  }

  @Override
  public void complete() throws IOException {
    encodingFactory.writeIsNotNull(true);
  }

  @Override
  public boolean isCompleted() throws IOException {
    return encodingFactory.readIsNotNull();
  }
}
