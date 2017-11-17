package com.elminster.easy.rpc.client;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.ConnectionException;
import com.elminster.easy.rpc.exception.RpcException;

/**
 * The RPC client interface.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcClient {

  /**
   * Get the connection endpoint.
   * 
   * @return the connection endpoint
   */
  public ConnectionEndpoint getConnectionEndpoint();

  /**
   * Get the encoding factory.
   * 
   * @return the encoding factory
   */
  public RpcEncodingFactory getEncodingFactory();

  /**
   * Get the RPC context;
   * 
   * @return the RPC context
   */
  public RpcContext getRpcContext();

  /**
   * Is using the secure connection?
   * 
   * @return is using the secure connection?
   */
  public boolean isSecureConnection();

  /**
   * Connect to the RPC server.
   * 
   * @throws RpcException
   *           on error
   */
  public void connect() throws ConnectionException;

  /**
   * Disconnect from the RPC server.
   */
  public void disconnect();

  /**
   * Is connected to the RPC server?
   * 
   * @return is connected to the RPC server?
   */
  public boolean isConnected();
  
  /**
   * Get version.
   * @return the version
   */
  public String getVersion();

  /**
   * Remote Method Call.
   * 
   * @param rpcCall
   *          the RPC call
   * @return result
   * @throws Throwable
   *           on error
   */
  public Object invokeService(RpcCall rpcCall) throws Throwable;
}
