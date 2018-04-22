package com.elminster.easy.rpc.exception;

public class CodecException extends RpcException {
  
  private static final long serialVersionUID = 1L;
  
  public CodecException(String string) {
    super(string);
  }

  public CodecException(String message, Throwable e) {
    super(message, e);
  }

}
