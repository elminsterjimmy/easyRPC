package com.elminster.easy.rpc.server.connection.impl;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.protocol.ResponseProtocol;

public interface NioRpcCall extends RpcCall {

  public NioRpcConnection getConnection();
  public ResponseProtocol getResponseProtocol();
}