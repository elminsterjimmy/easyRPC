package com.elminster.easy.rpc.client.container;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.context.ConnectionEndpoint;

/**
 * The container factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface ContainerFactory {

  public Container getContainer(RpcClient rpcClient, ConnectionEndpoint endpoint) throws ObjectInstantiationExcption; 
}