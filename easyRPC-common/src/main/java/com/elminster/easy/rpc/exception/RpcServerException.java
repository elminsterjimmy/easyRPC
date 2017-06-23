package com.elminster.easy.rpc.exception;

/**
 * The RPC Server Exception.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcServerException extends RpcException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final int errorCode;

  public RpcServerException(String msg, Throwable cause, int errorCode) {
    super(msg, cause);
    this.errorCode = errorCode;
  }

  public RpcServerException(String errorMessage, int errorCode) {
    super(errorMessage);
    this.errorCode = errorCode;
  }

  /**
   * Get the error code.
   * @return the error code
   */
  public int getErrorCode() {
    return errorCode;
  }
}
