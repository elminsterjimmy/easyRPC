package com.elminster.easy.rpc.registery;

import java.lang.reflect.InvocationTargetException;

import com.elminster.common.util.Assert;
import com.elminster.common.util.ReflectUtil;
import com.elminster.easy.rpc.connection.SocketFactory;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.exception.ObjectInstantiationExcption;

/**
 * The Socket Factory Registry.
 * 
 * @author jinggu
 * @version 1.0
 */
public class SocketFactoryRegsitery extends RegaistryBase<SocketFactory> implements Registrable<SocketFactory> {

  public static final SocketFactoryRegsitery INSTANCE = new SocketFactoryRegsitery();

  private SocketFactoryRegsitery() {
  }

  /**
   * Get socket factory.
   * 
   * @param context
   *          the RPC context
   * @return the socket factory
   */
  public SocketFactory getSocketFactory(RpcContext context) throws ObjectInstantiationExcption {
    Assert.notNull(context);
    String socketFactoryClassName = context.getSocketFactoryClassName();
    SocketFactory factory = findObject(socketFactoryClassName);
    return factory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void register(SocketFactory socketFactory) {
    Assert.notNull(socketFactory);
    cache.put(socketFactory.getClass().getName(), socketFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unregister(SocketFactory socketFactory) {
    cache.remove(socketFactory.getClass().getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected SocketFactory instaceObject(String socketFactoryClassName) throws ObjectInstantiationExcption {
    try {
      return (SocketFactory) ReflectUtil.newInstanceViaReflect(socketFactoryClassName);
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | ClassNotFoundException e) {
      throw new ObjectInstantiationExcption(e);
    }
  }
}
