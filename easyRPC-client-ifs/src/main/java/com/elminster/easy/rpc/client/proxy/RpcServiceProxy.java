package com.elminster.easy.rpc.client.proxy;

import com.elminster.easy.rpc.service.RpcService;

/**
 * The RPC service proxy interface.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcServiceProxy {

  /**
   * Make a service proxy for the RPC service.
   * @param rpcService the RPC service
   * @return the service proxy
   */
  public <T extends RpcService> T makeProxy(T rpcService);
}
