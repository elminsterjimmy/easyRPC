package com.elminster.easy.rpc.server;

import java.util.List;

import com.elminster.easy.rpc.codec.CoreCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.exception.ServerException;
import com.elminster.easy.rpc.server.listener.RpcServerListener;
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
   * 
   * @param serviceName
   *          the service name
   * @return the RPC service
   * @throws RpcException
   *           on error
   */
  public RpcService getService(final String serviceName) throws RpcException;

  /**
   * Server listen on the port.
   * 
   * @param port
   *          the port
   * @throws ServerException
   *           on error
   */
  public void listen(int port) throws ServerException;

  /**
   * Shutdown the server.
   * 
   * @param force
   *          if true force close all connections
   * @throws ServerException
   *           on error
   */
  public void shutdown(boolean force) throws ServerException;

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
   * @param coreCodec
   *          the core codec
   * @return the encoding factory
   */
  public RpcEncodingFactory getEncodingFactory(String encodingName, CoreCodec coreCodec);

  /**
   * Check the server uses secure connection?
   * 
   * @return if the server uses secure connection
   */
  public boolean useSecureConnection();

  /**
   * Set the server uses secure connection or not.
   * 
   * @param useSecure
   *          use secure conneciton or not
   */
  public void setUseSecureConnection(boolean useSecure);

  /**
   * Add a server listener.
   * 
   * @param listener
   *          the server listener
   */
  public void addServerListener(RpcServerListener listener);

  /**
   * Get the server listeners.
   * 
   * @return the server listeners
   */
  public List<RpcServerListener> getServerListeners();

  /**
   * Remove a server listener.
   * 
   * @param listener
   *          the server listener to remove
   */
  public void removeServerListener(RpcServerListener listener);

  /**
   * Get the RPC context.
   * 
   * @return the RPC context
   */
  public RpcContext getContext();

  /**
   * Get the RPC server version.
   * 
   * @return the RPC server version
   */
  public String getVersion();

  /**
   * Get open connection count.
   * 
   * @return open connection count
   */
  public int getOpenConnectionCount();
}
