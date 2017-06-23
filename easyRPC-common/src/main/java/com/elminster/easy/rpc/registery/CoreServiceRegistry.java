package com.elminster.easy.rpc.registery;

import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.util.RpcUtil;

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
  private volatile RpcUtil rpcUtil;

  /**
   * Get the RPCUtil.
   * @return the RPCUtil
   */
  public RpcUtil getRpcUtil() {
    Assert.notNull(rpcUtil);
    return this.rpcUtil;
  }

  /**
   * Set the RPCUtil.
   * @param RpcUtil the RPCUtil
   */
  public void setRpcUtil(RpcUtil RpcUtil) {
    this.rpcUtil = RpcUtil;
  }
}