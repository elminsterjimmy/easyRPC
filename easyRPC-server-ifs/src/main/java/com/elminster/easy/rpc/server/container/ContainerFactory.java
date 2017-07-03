package com.elminster.easy.rpc.server.container;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.server.RpcServer;

/**
 * The container factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface ContainerFactory {

  public Container getContainer(RpcServer rpcServer, ConnectionEndpoint endpoint) throws ObjectInstantiationExcption; 
}