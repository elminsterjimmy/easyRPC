package com.elminster.easy.rpc.server;

import com.elminster.easy.rpc.context.RpcContext;

/**
 * The RPC server factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcServerFactory {

  /**
   * Create the RPC server with the RPC context.
   * 
   * @param context
   *          the RPC context
   * @return the RPC server
   */
  public RpcServer createRpcServer(RpcContext context);
}
