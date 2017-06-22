package com.elminster.easy.rpc.codec.impl;

import com.elminster.easy.rpc.codec.CodecRepositoryElement;
import com.elminster.easy.rpc.codec.RpcCodec;

public class CodecRepositoryElementImpl implements CodecRepositoryElement {

  private final String idlName;
  private final String className;
  private final RpcCodec codec;

  public CodecRepositoryElementImpl(final String newIdlName, final String newClassName, final RpcCodec newCodec) {
    this.idlName = newIdlName;
    this.className = newClassName;
    this.codec = newCodec;
  }

  public String getIdlName() {
    return idlName;
  }

  public String getClassName() {
    return className;
  }

  public RpcCodec getCodec() {
    return codec;
  }
}
