package com.elminster.easy.rpc.protocol;

import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.ObjectInstantiationExcption;

/**
 * Protocol Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface ProtocolFactory {

  /**
   * Create the protocol.
   * 
   * @param protocolClass
   *          the protocol class
   * @param encodingFactory
   *          the encoding factory
   * @return the protocol
   * @throws ObjectInstantiationExcption
   *          on create error
   */
  public Protocol createProtocol(Class<? extends Protocol> protocolClass, RpcEncodingFactory encodingFactory) throws ObjectInstantiationExcption;
}
