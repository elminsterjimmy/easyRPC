package com.elminster.easy.rpc.registery;

import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.codec.CoreCodec;

/**
 * A Service Registry for injection.
 * 
 * @author jinggu
 * @version 1.0
 */
public class CoreServiceRegistry {

  /** singleton instance. */
  public static final CoreServiceRegistry INSTANCE = new CoreServiceRegistry();

  /** the RPCUtil. */
  private volatile CoreCodec rpcUtil;
  
  /**
   * Get the RPCUtil.
   * @return the RPCUtil
   */
  public CoreCodec getRpcUtil() {
    Assert.notNull(rpcUtil);
    return this.rpcUtil;
  }

  /**
   * Set the RPCUtil.
   * @param RpcUtil the RPCUtil
   */
  public void setRpcUtil(CoreCodec RpcUtil) {
    this.rpcUtil = RpcUtil;
  }
}