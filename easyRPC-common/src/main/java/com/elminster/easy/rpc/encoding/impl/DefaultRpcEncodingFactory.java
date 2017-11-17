package com.elminster.easy.rpc.encoding.impl;

/**
 * Default RPC Encoding Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public class DefaultRpcEncodingFactory extends RpcEncodingFactoryBase {

  /** Default encoding factory name: {@literal _default_} */
  public static final String ENCODING_FACTORY_NAME = "_default_";

  /**
   * Constructor.
   */
  public DefaultRpcEncodingFactory() {
    super(ENCODING_FACTORY_NAME);
  }
}
