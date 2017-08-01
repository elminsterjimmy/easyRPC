package com.elminster.easy.rpc.server.connection.impl;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.impl.RpcCallImpl;
import com.elminster.easy.rpc.protocol.ResponseProtocol;

public class NioRpcCallImpl extends RpcCallImpl implements NioRpcCall {

  private final NioRpcConnection connection;
  private final ResponseProtocol responseProtocol;

  public NioRpcCallImpl(RpcCall rpcCall, NioRpcConnection connection, ResponseProtocol responseProtocol) {
    super(rpcCall.getRequestId(), rpcCall.isAsyncCall(), rpcCall.getServiceName(), rpcCall.getMethodName(), rpcCall.getArgs(), rpcCall.getContext(), rpcCall.getPriority(),
        rpcCall.getTimeout());
    if (null != rpcCall.getInvokeEndAt()) {
      rpcCall.setInvokeEndAt(rpcCall.getInvokeEndAt());
    }
    if (null != rpcCall.getInvokeStartAt()) {
      rpcCall.setInvokeStartAt(rpcCall.getInvokeStartAt());
    }
    rpcCall.setResult(rpcCall.getResult());
    if (null != rpcCall.getRpcCallEndAt()) {
      rpcCall.setRpcCallEndAt(rpcCall.getRpcCallEndAt());
    }
    if (null != rpcCall.getRpcCallStartAt()) {
      rpcCall.setRpcCallStartAt(rpcCall.getRpcCallStartAt());
    }
    rpcCall.setStatus(rpcCall.getStatus());
    this.connection = connection;
    this.responseProtocol = responseProtocol;
  }

  @Override
  public NioRpcConnection getConnection() {
    return connection;
  }

  @Override
  public ResponseProtocol getResponseProtocol() {
    return responseProtocol;
  }

}
