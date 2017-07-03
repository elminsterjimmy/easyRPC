package com.elminster.easy.rpc.server.container.impl;

import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class BioContainer extends ContainerBase implements Container {
  
  private static final Logger logger = LoggerFactory.getLogger(BioContainer.class);

  private static final int RETRY_THRESHOLD = 15;
  private static final int RETRY_INTERVAL = 500;
  
  private volatile boolean isStop = true;

  public BioContainer(RpcServer rpcServer, ConnectionEndpoint endpoint) {
    super(rpcServer, endpoint);
  }

  @Override
  protected void startWorkerThreads() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void serve() throws Exception {
    ServerListener listener = ServerListenerFactory.INSTANCE.getServerListener(rpcServer, endpoint);
    listener.listen();
    setServing(true);
    
    isStop = false;
    while (!isStop) {
      try {
        RpcConnection connection = listener.accept();
        int retryCnt = 0;
        while (true) {
          ThreadPoolExecutor threadpool = getAsyncWorkerThreadPool();
          try {
            threadpool.execute(connection);
            break;
          } catch (RejectedExecutionException e) {
            // TODO retry
            logger.warn(String.format("Failed to push connection [%s] to thread pool [%s]. Cause: %s.", connection, threadpool, e));
            if (retryCnt++ < RETRY_THRESHOLD) {
              logger.debug(String.format("Try to repush connection [%s] to thread pool [%s] after %d ms", connection, threadpool, RETRY_INTERVAL));
              Thread.sleep(RETRY_INTERVAL);
            } else {
              logger.error(String.format("Retry many times to push connection [%s] to thread pool [%s] but failed, drop connection!!", connection, threadpool));
            }
          }
        }
      } catch (IOException e) {
        ;
      }
    }
    setServing(false);
  }

  @Override
  protected void stopServe() throws Exception {
    isStop = true;
  }

}
