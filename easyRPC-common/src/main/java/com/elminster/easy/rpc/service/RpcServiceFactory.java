package com.elminster.easy.rpc.service;

import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.exception.ConnectionException;

/**
 * The RPC Service Factory.
 * 
 * @author jinggu
 *
 * @param <T>
 *          the service type
 * @version 1.0
 */
public interface RpcServiceFactory<T> {

  /**
   * Pop a cached RPC service.
   * 
   * @param endpoint
   *          the connection endpoint
   * @return the RPC service with specified endpoint
   * @throws ConnectionException
   *           on error
   */
  public T popService(ConnectionEndpoint endpoint) throws ConnectionException;

  /**
   * Push back a RPC service to the cache.
   * 
   * @param service
   *          the RPC service
   * @throws ConnectionException
   *           on error
   */
  public void pushService(T service) throws ConnectionException;

  /**
   * Shutdown the service factory.
   * 
   * @throws InterruptedException
   *           on error
   */
  public void shutdown() throws InterruptedException;
}
