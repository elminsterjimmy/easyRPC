package com.elminster.easy.rpc.client.exception;

public class RequestIdNotMatchException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public RequestIdNotMatchException(String expact, String actual) {
    super(String.format("Expect Request Id [%s] but Actual Request Id [%s].", expact, actual));
  }

}
