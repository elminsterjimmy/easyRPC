package com.elminster.easy.rpc.server.container.listener.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.elminster.common.util.ReflectUtil;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.exception.ObjectInstantiationExcption;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.listener.ServerListener;
import com.elminster.easy.rpc.server.listener.RpcServerListenerFactory;

/**
 * The Server Listener Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ServerListenerFactory implements RpcServerListenerFactory {

  public static ServerListenerFactory INSTANCE = new ServerListenerFactory();

  private ServerListenerFactory() {
  }

  @SuppressWarnings("unchecked")
  public ServerListener getServerListener(RpcServer rpcServer, ConnectionEndpoint endpoint)
      throws ObjectInstantiationExcption {
    RpcContext context = rpcServer.getContext();
    String serverlistenerClassName = context.getServerListenerClassName();
    Class<? extends ServerListener> clazz;
    try {
      clazz = (Class<? extends ServerListener>) ReflectUtil.forName(serverlistenerClassName);
      Constructor<? extends ServerListener> constructor = ReflectUtil.getConstructor(clazz, RpcServer.class, ConnectionEndpoint.class);
      ServerListener listener = constructor.newInstance(rpcServer, endpoint);
      return listener;
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new ObjectInstantiationExcption(e);
    }
  }
}
