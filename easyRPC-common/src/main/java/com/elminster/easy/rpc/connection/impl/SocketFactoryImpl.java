package com.elminster.easy.rpc.connection.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.elminster.common.util.Assert;
import com.elminster.common.util.ReflectUtil;
import com.elminster.easy.rpc.connection.SocketFactory;
import com.elminster.easy.rpc.context.Configurable;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.registery.Registrable;

public class SocketFactoryImpl implements SocketFactory, Configurable, Registrable<SocketFactory> {

  private RpcContext context;

  private Map<String, SocketFactory> socketFactories = new ConcurrentHashMap<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public ServerSocket createServerSocket(int port, boolean useSecure) throws IOException {
    return findSocketFactory().createServerSocket(port, useSecure);
  }
  
  private SocketFactory findSocketFactory() {
    Assert.notNull(context);
    String socketFactoryClassName = context.getSocketFactoryClassName();
    SocketFactory factory = socketFactories.get(socketFactoryClassName);
    if (null == factory) {
      try {
        factory = (SocketFactory) ReflectUtil.newInstanceViaReflect(socketFactoryClassName);
        socketFactories.put(socketFactoryClassName, factory);
      } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
          | ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return factory;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createClientSocket(ConnectionEndpoint connectionEndpoint) throws IOException {
    return findSocketFactory().createClientSocket(connectionEndpoint);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setContext(RpcContext context) {
    this.context = context;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcContext getContext() {
    return context;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void register(SocketFactory socketFactory) {
    Assert.notNull(socketFactory);
    socketFactories.put(socketFactory.getClass().getName(), socketFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unregister(SocketFactory socketFactory) {
    socketFactories.remove(socketFactory.getClass().getName());
  }
}
