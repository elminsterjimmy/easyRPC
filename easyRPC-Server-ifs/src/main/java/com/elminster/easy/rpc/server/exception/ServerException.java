package com.elminster.easy.rpc.server.exception;

public class ServerException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ServerException(String msg, Throwable t) {
    super(msg, t);
  }

  public ServerException(Exception e) {
    super(e);
  }

}
