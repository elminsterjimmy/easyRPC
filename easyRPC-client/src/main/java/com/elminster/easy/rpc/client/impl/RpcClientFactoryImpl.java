package com.elminster.easy.rpc.client.impl;

import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.client.RpcClientFactory;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.codec.impl.RpcEncodingFactoryBase;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;

public class RpcClientFactoryImpl implements RpcClientFactory {

  public static final RpcClientFactory INSTANCE = new RpcClientFactoryImpl();

  private RpcClientFactoryImpl() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcClient createRpcClient(ConnectionEndpoint endpoint, RpcContext context) {
    return new RpcClientImpl(endpoint, new RpcEncodingFactoryBase("default"), context);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcClient createRpcClient(ConnectionEndpoint endpoint, RpcEncodingFactory encodingFactory, RpcContext context) {
    return new RpcClientImpl(endpoint, encodingFactory, context);
  }
}