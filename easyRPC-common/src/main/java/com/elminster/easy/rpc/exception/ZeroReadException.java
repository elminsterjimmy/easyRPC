package com.elminster.easy.rpc.exception;

import java.io.IOException;

public class ZeroReadException extends IOException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ZeroReadException(String message) {
    super(message);
  }
}
