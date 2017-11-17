package com.elminster.easy.rpc.protocol.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.util.Assert;
import com.elminster.common.util.ReflectUtil;
import com.elminster.easy.rpc.protocol.AsyncRequestProtocol;
import com.elminster.easy.rpc.protocol.AsyncResponseProtocol;
import com.elminster.easy.rpc.protocol.Protocol;
import com.elminster.easy.rpc.protocol.ProtocolFactory;
import com.elminster.easy.rpc.protocol.RequestHeaderProtocol;
import com.elminster.easy.rpc.protocol.RequestProtocol;
import com.elminster.easy.rpc.protocol.RequestTypeProtocol;
import com.elminster.easy.rpc.protocol.ResponseHeaderProtocol;
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
  
  private static final Map<Class<? extends Protocol<?>>, Class<? extends Protocol<?>>> interfaces2classes = new ConcurrentHashMap<>(7);
  
  static {
    // TODO maybe use custom cl.
    interfaces2classes.put(ShakehandProtocol.class, ShakehandProtocolImpl.class);
    interfaces2classes.put(VersionProtocol.class, VersionProtocolImpl.class);
    interfaces2classes.put(RequestHeaderProtocol.class, RequestHeaderProtocolImpl.class);
    interfaces2classes.put(RequestTypeProtocol.class, RequestTypeProtocolImpl.class);
    interfaces2classes.put(RequestProtocol.class, RequestProtocolImpl.class);
    interfaces2classes.put(AsyncRequestProtocol.class, AsyncRequestProtocolImpl.class);
    interfaces2classes.put(ResponseHeaderProtocol.class, ResponseHeaderProtocolImpl.class);
    interfaces2classes.put(ResponseProtocol.class, ResponseProtocolImpl.class);
    interfaces2classes.put(AsyncResponseProtocol.class, AsyncResponseProtocolImpl.class);
  }
  
  private ProtocolFactoryImpl() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Protocol<?> createProtocol(Class<? extends Protocol<?>> protocolClass) throws ObjectInstantiationExcption {
    Assert.notNull(protocolClass);
    try {
      Class<? extends Protocol<?>> impl = interfaces2classes.get(protocolClass);
      Constructor<? extends Protocol<?>> constructor = ReflectUtil.getConstructor(impl);
      Protocol<?> protocol = constructor.newInstance();
      return protocol;
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new ObjectInstantiationExcption(e);
    }
  }
}
