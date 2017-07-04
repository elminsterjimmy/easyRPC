package com.elminster.easy.rpc.protocol.impl;

import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.protocol.Protocol;

abstract public class ProtocolImpl implements Protocol {

  protected final RpcEncodingFactory encodingFactory;
  
  public ProtocolImpl(RpcEncodingFactory encodingFactory) {
    this.encodingFactory = encodingFactory;
  }
}
