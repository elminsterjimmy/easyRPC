package com.elminster.easy.rpc.exception;

import java.io.IOException;

public class ConnectionException extends IOException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ConnectionException(String msg, Throwable t) {
    super(msg, t);
  }
}
