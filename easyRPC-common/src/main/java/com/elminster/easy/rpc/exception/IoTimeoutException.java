package com.elminster.easy.rpc.exception;

import java.io.IOException;

public class IoTimeoutException extends IOException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public IoTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }

  public IoTimeoutException(String message) {
    super(message);
  }
}
