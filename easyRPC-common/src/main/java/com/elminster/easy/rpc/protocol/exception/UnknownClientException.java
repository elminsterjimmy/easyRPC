package com.elminster.easy.rpc.protocol.exception;

import com.elminster.easy.rpc.exception.RpcException;

public class UnknownClientException extends RpcException {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public UnknownClientException(String string) {
    super(string);
  }
}
