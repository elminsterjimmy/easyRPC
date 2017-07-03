package com.elminster.easy.rpc.server.processor;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.easy.rpc.server.RpcServer;

public interface RpcServiceProcessorFactory {

  public RpcServiceProcessor createServiceProcessor(RpcServer rpcServer) throws ObjectInstantiationExcption;
}
