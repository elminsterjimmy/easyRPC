package com.elminster.easy.rpc.server.container;

import com.elminster.easy.rpc.server.exception.ServerException;

/**
 * Network Container.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface Container {

  /**
   * Start the network container on the port.
   * 
   * @param port
   *          the port
   * @param useSecure
   *          use secure connection?
   */
  public void start(int port, boolean useSecure) throws ServerException;
  
  /**
   * Use secure or not?
   * @return use secure or not
   */
  public boolean isUseSecure();

  /**
   * Stop the network container.
   * 
   * @throws ServerException
   *           on error
   */
  public void stop() throws ServerException;
}
