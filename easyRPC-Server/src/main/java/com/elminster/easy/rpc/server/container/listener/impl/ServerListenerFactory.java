package com.elminster.easy.rpc.server.container.listener.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.elminster.common.util.ReflectUtil;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.listener.ServerListener;

/**
 * The Server Listener Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ServerListenerFactory {

  public static ServerListenerFactory INSTANCE = new ServerListenerFactory();

  /** the cache. */
  private Map<String, Class<? extends ServerListener>> classCache = new ConcurrentHashMap<>();

  private ServerListenerFactory() {
  }

  @SuppressWarnings("unchecked")
  public ServerListener getServerListener(RpcServer rpcServer, ConnectionEndpoint endpoint)
      throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
    RpcContext context = rpcServer.getContext();
    String serverlistenerClassName = context.getServerListenerClassName();
    Class<? extends ServerListener> clazz = classCache.get(serverlistenerClassName);
    if (null == clazz) {
      clazz = (Class<? extends ServerListener>) ReflectUtil.forName(serverlistenerClassName);
      classCache.put(serverlistenerClassName, clazz);
    }
    Constructor<? extends ServerListener> constructor = ReflectUtil.getConstructor(clazz, RpcServer.class, ConnectionEndpoint.class);
    ServerListener listener = constructor.newInstance(rpcServer, endpoint);
    return listener;
  }
}
