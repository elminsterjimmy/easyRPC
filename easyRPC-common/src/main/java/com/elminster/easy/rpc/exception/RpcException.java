package com.elminster.easy.rpc.exception;

public class RpcException extends Exception {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private String requestId;
  private String causedByClassName;
  private String causedByStackTrace;

  public RpcException(String string) {
    super(string);
  }

  public RpcException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
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
