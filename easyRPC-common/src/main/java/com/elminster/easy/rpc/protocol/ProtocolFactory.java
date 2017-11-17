package com.elminster.easy.rpc.protocol;

import com.elminster.common.exception.ObjectInstantiationExcption;

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
   * @return the protocol
   * @throws ObjectInstantiationExcption
   *          on create error
   */
  public Protocol<?> createProtocol(Class<? extends Protocol<?>> protocolClass) throws ObjectInstantiationExcption;
}
