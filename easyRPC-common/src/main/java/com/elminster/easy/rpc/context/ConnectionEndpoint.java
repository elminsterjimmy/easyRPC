package com.elminster.easy.rpc.context;

/**
 * The connection endpoint.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface ConnectionEndpoint {

  /**
   * Get the host.
   * 
   * @return the host
   */
  public String getHost();

  /**
   * Get the port.
   * 
   * @return the port
   */
  public Integer getPort();

  /**
   * Use secure socket or not?
   * 
   * @return use secure socket or not
   */
  public Boolean useSecureSocket();
}
