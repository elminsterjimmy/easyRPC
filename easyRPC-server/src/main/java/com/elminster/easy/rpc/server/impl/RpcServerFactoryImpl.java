package com.elminster.easy.rpc.server.impl;

import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.RpcServerFactory;

/**
 * The RPC server factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcServerFactoryImpl implements RpcServerFactory {

  public static final RpcServerFactory INSTANCE = new RpcServerFactoryImpl();

  private RpcServerFactoryImpl() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcServer createRpcServer(RpcContext context) {
    return new RpcServerImpl(context);
  }
}
