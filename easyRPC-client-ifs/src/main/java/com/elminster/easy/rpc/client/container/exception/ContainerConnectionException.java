package com.elminster.easy.rpc.client.container.exception;

public class ContainerConnectionException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ContainerConnectionException(String string, Exception e) {
    super(string, e);
  }
}
