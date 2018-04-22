package com.elminster.easy.rpc.client.exception;

import com.elminster.easy.rpc.exception.RpcException;

public class AsyncCallNoAckException extends RpcException {

  private static final long serialVersionUID = 1L;

  public AsyncCallNoAckException(String string) {
    super(string);
  }
}
