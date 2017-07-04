package com.elminster.easy.rpc.protocol.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.util.Assert;
import com.elminster.common.util.ReflectUtil;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol;
import com.elminster.easy.rpc.protocol.Protocol;
import com.elminster.easy.rpc.protocol.ProtocolFactory;
import com.elminster.easy.rpc.protocol.RequestHeaderProtocol;
import com.elminster.easy.rpc.protocol.RequestProtocol;
import com.elminster.easy.rpc.protocol.ResponseProtocol;
import com.elminster.easy.rpc.protocol.ShakehandProtocol;
import com.elminster.easy.rpc.protocol.VersionProtocol;

/**
 * The Protocol Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ProtocolFactoryImpl implements ProtocolFactory {

  public static final ProtocolFactory INSTANCE = new ProtocolFactoryImpl();
  
  private static final Map<Class<? extends Protocol>, Class<? extends Protocol>> interfaces2classes = new ConcurrentHashMap<>(5);
  
  static {
    // TODO maybe use custom cl.
    interfaces2classes.put(ShakehandProtocol.class, ShakehandProtocolImpl.class);
    interfaces2classes.put(VersionProtocol.class, VersionProtocolImpl.class);
    interfaces2classes.put(RequestHeaderProtocol.class, RequestHeaderProtocalImpl.class);
    interfaces2classes.put(RequestProtocol.class, RequestProtocolImpl.class);
    interfaces2classes.put(ResponseProtocol.class, ResponseProtocolImpl.class);
    interfaces2classes.put(ConfirmFrameProtocol.class, ConfirmFrameProtocolImpl.class);
  }
  
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
      Class<? extends Protocol> impl = interfaces2classes.get(protocolClass);
      Constructor<? extends Protocol> constructor = ReflectUtil.getConstructor(impl, RpcEncodingFactory.class);
      Protocol protocol = constructor.newInstance(encodingFactory);
      return protocol;
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new ObjectInstantiationExcption(e);
    }
  }
}
