package com.elminster.easy.rpc.client.proxy;

import com.elminster.easy.rpc.client.RpcClient;

/**
 * The RPC interface proxy.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcProxy {

  /**
   * Make a proxy for the RPC interface.
   * 
   * @param rpcInterface
   *          the RPC interface class
   * @param rpcClient
   *          the RPC client
   * @return the RPC interface proxy
   */
  public <T> T makeProxy(Class<T> rpcInterface, RpcClient rpcClient);
}
