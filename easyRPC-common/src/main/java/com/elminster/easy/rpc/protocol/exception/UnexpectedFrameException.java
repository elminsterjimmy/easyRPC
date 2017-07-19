package com.elminster.easy.rpc.protocol.exception;

import java.io.IOException;

import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol.Frame;

public class UnexpectedFrameException extends IOException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public UnexpectedFrameException(byte expect, byte actual) {
    super(String.format("Unexpected Frame! Expact frame is [%s] but got [%s]!", Frame.getFrame(expect).name(), Frame.getFrame(actual).name()));
  }

}
