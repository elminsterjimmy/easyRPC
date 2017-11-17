package com.elminster.easy.rpc.encoding;

import com.elminster.easy.rpc.exception.RpcException;

/**
 * RPC Encoding Factory Not Found Exception.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcEncodingFactoryNotFoundException extends RpcException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public RpcEncodingFactoryNotFoundException(String encodingFactoryName) {
    super(String.format("RPC Encoding Factory [%s] is not available.", encodingFactoryName));
  }
}
