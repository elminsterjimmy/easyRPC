package com.elminster.easy.rpc.protocol.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.util.Assert;
import com.elminster.common.util.ReflectUtil;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.protocol.Protocol;
import com.elminster.easy.rpc.protocol.ProtocolFactory;

/**
 * The Protocol Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ProtocolFactoryImpl implements ProtocolFactory {

  public static final ProtocolFactory INSTANCE = new ProtocolFactoryImpl();

  private ProtocolFactoryImpl() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Protocol createProtocol(Class<? extends Protocol> protocolClass, RpcEncodingFactory encodingFactory) throws ObjectInstantiationExcption {
    Assert.notNull(protocolClass);
    Assert.notNull(encodingFactory);
    try {
      Constructor<? extends Protocol> constructor = ReflectUtil.getConstructor(protocolClass, RpcEncodingFactory.class);
      Protocol protocol = constructor.newInstance(encodingFactory);
      return protocol;
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new ObjectInstantiationExcption(e);
    }
  }

}
