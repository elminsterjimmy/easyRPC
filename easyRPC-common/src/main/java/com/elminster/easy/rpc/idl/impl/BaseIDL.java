package com.elminster.easy.rpc.idl.impl;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.idl.IDL;

public class BaseIDL implements IDL {

  private final String localName;
  private final String remoteName;
  private final Class<?> clazz;
  private final Class<? extends RpcCodec> codec;
  
  public BaseIDL(String local, String remote, Class<?> typeClass, Class<? extends RpcCodec> codecClass) {
    this.localName = local;
    this.remoteName = remote;
    this.clazz = typeClass;
    this.codec = codecClass;
  }

  public String getLocalName() {
    return this.localName;
  }

  public String getRemoteName() {
    return this.remoteName;
  }

  public Class<?> getTypeClass() {
    return this.clazz;
  }

  public Class<? extends RpcCodec> getCodecClass() {
    return this.codec;
  }
}
