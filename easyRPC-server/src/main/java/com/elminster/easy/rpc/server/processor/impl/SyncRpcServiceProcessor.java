package com.elminster.easy.rpc.server.processor.impl;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;
import com.elminster.easy.rpc.service.RpcService;

/**
 * RPC sync service processor.
 * 
 * @author jinggu
 * @version 1.0
 */
public class SyncRpcServiceProcessor extends RpcServiceProcessorBase implements RpcServiceProcessor {
  
//  private static final Logger logger = LoggerFactory.getLogger(SyncRpcServiceProcessor.class);
  
  public SyncRpcServiceProcessor(RpcServer rpcServer) {
    super(rpcServer);
  }
  
  private void invokeServiceMethod(RpcCall rpcCall) throws RpcException {
    RpcService service = getRpcService(rpcCall);
    invokeInternal(service, rpcCall);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void invoke(RpcCall call) throws RpcException {
    invokeServiceMethod(call);
    putProcessedCall(call);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcCall getResult(String requestId, long timeout) {
    return processedRpcCalls.remove(requestId);
  }
}
