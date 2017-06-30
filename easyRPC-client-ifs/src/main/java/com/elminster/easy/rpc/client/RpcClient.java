package com.elminster.easy.rpc.client;

import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
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
  public void connect() throws RpcException;

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
   * Is it a stay connection long or just a one call connection?
   * 
   * @return Is it a connection stay long or just a one call connection?
   */
  public boolean isLongConnection();
}
