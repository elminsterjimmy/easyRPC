package com.elminster.easy.rpc.service;

/**
 * The RPC Service.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcService {

  /**
   * Get the service methods.
   * 
   * @return the service methods
   */
  public String[] getServiceMethods();

  /**
   * Get service name.
   * 
   * @return the service name
   */
  public String getServiceName();

  /**
   * Get service version.
   * 
   * @return the service version
   */
  public String getServiceVersion();
}
