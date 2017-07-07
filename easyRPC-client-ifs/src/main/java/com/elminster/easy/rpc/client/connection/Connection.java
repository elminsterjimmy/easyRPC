package com.elminster.easy.rpc.client.connection;

import java.io.IOException;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.exception.ConnectionException;

public interface Connection {

  public void connect() throws ConnectionException;
  
  public void disconnect() throws IOException;

  public Object invokeService(RpcCall rpcCall) throws Throwable;

  public boolean isConnected();
}
