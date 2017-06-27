package com.elminster.easy.rpc.server.container.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.RejectedExecutionException;

import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.listener.ServerListener;
import com.elminster.easy.rpc.server.container.listener.impl.ServerListenerFactory;

/**
 * The Socket Container.
 * 
 * @author jinggu
 * @version 1.0
 */
public class SocketContainer extends ContainerBase implements Container {

  private volatile boolean isStop = true;

  public SocketContainer(RpcServer rpcServer, ConnectionEndpoint endpoint) {
    super(rpcServer, endpoint);
  }

  @Override
  protected void startWorkerThreads() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void serve() throws Exception {
    ServerListener listener;
    try {
      listener = ServerListenerFactory.INSTANCE.getServerListener(rpcServer, endpoint);
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | ClassNotFoundException e1) {
      throw new Exception("Server Listener cannot be found.", e1);
    }
    setServing(true);
    
    isStop = false;
    while (!isStop) {
      try {
        RpcConnection connection = listener.accept();
        while (true) {
          try {
            getAsyncWorkerThreadPool().execute(connection);
            break;
          } catch (RejectedExecutionException e) {
            // TODO retry
          }
        }
      } catch (Exception e) {
      }
    }
    setServing(false);
  }

  @Override
  protected void stopServe() throws Exception {
    isStop = true;
  }

}
