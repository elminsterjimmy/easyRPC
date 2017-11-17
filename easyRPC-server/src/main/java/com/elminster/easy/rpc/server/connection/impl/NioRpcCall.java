package com.elminster.easy.rpc.server.connection.impl;

import com.elminster.easy.rpc.call.RpcCall;

public interface NioRpcCall extends RpcCall {

  public NioRpcConnection getConnection();
}