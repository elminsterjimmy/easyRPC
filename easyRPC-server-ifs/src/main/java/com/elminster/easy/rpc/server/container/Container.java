package com.elminster.easy.rpc.server.container;

import java.util.concurrent.ThreadPoolExecutor;

import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.server.container.exception.StartContainerException;
import com.elminster.easy.rpc.server.container.exception.StopContainerException;

/**
 * Network Container.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface Container {

  /**
   * Start the network container and listen.
   */
  public void start() throws StartContainerException;

  /**
   * Use secure or not?
   * 
   * @return use secure or not
   */
  public boolean isUseSecure();

  /**
   * Stop the network container.
   * 
   * @param closeConnections
   *          close open connections?
   * @throws StopContainerException
   *           on error
   */
  public void stop(boolean closeConnections) throws StopContainerException;

  /**
   * Check if the container is serving?
   * 
   * @return if the container is serving?
   */
  public boolean isServing();
  
  /**
   * Get number of open connections.
   * 
   * @return number of open connections
   */
  public int getNumberOfOpenConnections();

  /**
   * Get the worker thread pool.
   * 
   * @return the worker thread pool
   */
  public ThreadPoolExecutor getAsyncWorkerThreadPool();
  
  /**
   * Get the Connection Endpoint.
   * @return the connection endpoint
   */
  public ConnectionEndpoint getConnectionEndpoint();

  public void addOpenConnection(RpcConnection connection);
  
  public void removeOpenConnection(RpcConnection connection);
}
