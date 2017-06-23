package com.elminster.easy.rpc.server;

import java.util.concurrent.ThreadPoolExecutor;

import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.service.RpcService;

/**
 * The RPC Server.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcServer {

  /**
   * Add an encoding factory.
   * 
   * @param encodingFactory
   *          the encoding factory
   */
  public void addEncodingFactory(final RpcEncodingFactory encodingFactory);

  /**
   * Add a RPC service.
   * 
   * @param rpcService
   *          the RPC service
   * @throws RpcException
   *           on error
   */
  public void addService(final RpcService rpcService) throws RpcException;
  
  /**
   * Get a RPC service by name.
   * @param serviceName the service name
   * @return the RPC service
   * @throws RpcException on error
   */
  public RpcService getService(final String serviceName) throws RpcException;

  /**
   * Server listen on the port.
   * 
   * @param port
   *          the port
   * @throws RpcException
   *           on error
   */
  public void listen(int port) throws RpcException;

  /**
   * Shutdown the server.
   * 
   * @param force
   *          if true force close all connections
   */
  public void shutdown(boolean force);

  /**
   * Get number of open connections.
   * 
   * @return number of open connections
   */
  public int getNumberOfOpenConnections();

  public ThreadPoolExecutor getAsyncWorkerThreadPool();

  /**
   * Do the version check or not.
   * 
   * @return do the version check or not
   */
  public boolean isVersionCheck();

  /**
   * Set the version check flag.
   * 
   * @param versionCheck
   *          the version check flag
   */
  public void setVersionCheck(boolean versionCheck);

  /**
   * Get specified encoding factory.
   * 
   * @param encodingName
   *          the encodeing name
   * @return the encoding factory
   */
  public RpcEncodingFactory getEncodingFactory(String encodingName);

  /**
   * Remove an open connection.
   * 
   * @param connection
   *          the open connection
   */
  public void removeOpenConnection(final RpcConnection connection);

  /**
   * Add an open connection.
   * 
   * @param connection
   *          the open connection
   */
  public void addOpenConnection(final RpcConnection connection);

  /**
   * Check the server uses secure connection?
   * 
   * @return if the server uses secure connection
   */
  public boolean isSecureConnection();

  /**
   * Set the server uses secure connection or not.
   * 
   * @param useSecure
   *          use secure conneciton or not
   */
  public void setUseSecureConnection(boolean useSecure);
}
