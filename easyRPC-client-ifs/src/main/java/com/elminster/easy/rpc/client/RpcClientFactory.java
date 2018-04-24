package com.elminster.easy.rpc.client;

import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;

/**
 * The RPC client factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcClientFactory {

  /**
   * Create a RPC client.
   * 
   * @param endpoint
   *          the endpoint
   * @param context
   *          the context
   * @return a RPC client
   */
  public RpcClient createRpcClient(ConnectionEndpoint endpoint, RpcContext context, boolean stayConnction);

  /**
   * Create a RPC client.
   * 
   * @param endpoint
   *          the endpoint
   * @param encodingFactory
   *          the encoding factory
   * @param context
   *          the context
   * @return a RPC client
   */
  public RpcClient createRpcClient(ConnectionEndpoint endpoint, RpcEncodingFactory encodingFactory, RpcContext context, boolean stayConnction);

  /**
   * Duplicate the RPC client.
   * 
   * @param client
   *          the RPC client to duplicate
   * @return duplicated RPC client
   */
  public RpcClient duplicateRpcClient(RpcClient client);

}
