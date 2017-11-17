package com.elminster.easy.rpc.request;

/**
 * The Response.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface Response {
  
  public String getRequestId();
  
  public String getEncoding();
  
  public boolean isVoidCall();
  
  public Object getReturnValue();
  
  public boolean isCancelled();
  
  public boolean isTimedout();
}
