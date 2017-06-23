package com.elminster.easy.rpc.exception;

public class RpcException extends Exception {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private String causedByClassName;
  private String causedByStackTrace;

  public RpcException(String string) {
    super(string);
  }

  public RpcException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public String getCausedByClassName() {
    return causedByClassName;
  }

  public void setCausedByClassName(String causedByClassName) {
    this.causedByClassName = causedByClassName;
  }

  public String getCausedByStackTrace() {
    return causedByStackTrace;
  }

  public void setCausedByStackTrace(String causedByStackTrace) {
    this.causedByStackTrace = causedByStackTrace;
  }
}
