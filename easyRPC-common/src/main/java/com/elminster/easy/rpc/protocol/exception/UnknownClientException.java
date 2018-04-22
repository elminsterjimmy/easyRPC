package com.elminster.easy.rpc.protocol.exception;

import com.elminster.easy.rpc.exception.CodecException;

public class UnknownClientException extends CodecException {
  
  private static final long serialVersionUID = 1L;

  public UnknownClientException(String string) {
    super(string);
  }
}
