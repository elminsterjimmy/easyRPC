package com.elminster.easy.rpc.exception;

/**
 * ObjectInstantiationExcption for factories.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ObjectInstantiationExcption extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ObjectInstantiationExcption(String message, Throwable cause) {
    super(message, cause);
  }

  public ObjectInstantiationExcption(String message) {
    super(message);
  }

  public ObjectInstantiationExcption(Throwable cause) {
    super(cause);
  }
}
