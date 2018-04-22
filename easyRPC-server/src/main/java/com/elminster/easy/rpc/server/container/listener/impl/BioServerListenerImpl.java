package com.elminster.easy.rpc.server.container.listener.impl;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.impl.SimpleConnectionEndpoint;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.connection.impl.BioRpcConnection;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.listener.RpcServerAcceptEvent;
import com.elminster.easy.rpc.server.listener.RpcServerListener;

/**
 * The BIO server listener.
 * 
 * @author jinggu
 * @version 1.0
 */
public class BioServerListenerImpl extends ServerListenerBase {

  private static final Logger logger = LoggerFactory.getLogger(BioServerListenerImpl.class);

  private static final int RETRY_THRESHOLD = 15;
  private static final long RETRY_INTERVAL = 1000;
  
  private volatile boolean stop = false;

  public BioServerListenerImpl(RpcServer rpcServer, Container container, ConnectionEndpoint endpoint) {
    super(rpcServer, container, endpoint);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void listen() throws IOException {
    super.listen();
    stop = false;
    while (!stop) {
      try {
        RpcConnection connection = accept();
        int retryCnt = 0;
        while (true) {
          ThreadPoolExecutor threadpool = container.getAsyncWorkerThreadPool();
          try {
            threadpool.execute(connection);
            container.addOpenConnection(connection);
            break;
          } catch (RejectedExecutionException e) {
            // retry
            logger.warn(String.format("Failed to push connection [%s] to thread pool [%s]. Cause: %s.", connection, threadpool, e));
            if (retryCnt++ < RETRY_THRESHOLD) {
              logger.debug(String.format("Try to repush connection [%s] to thread pool [%s] after %d ms", connection, threadpool, RETRY_INTERVAL));
              try {
                Thread.sleep(RETRY_INTERVAL);
              } catch (InterruptedException e1) {
                ;
              }
            } else {
              logger.error(String.format("Retry many times to push connection [%s] to thread pool [%s] but failed, drop connection!!", connection, threadpool));
            }
          }
        }
      } catch (IOException e) {
        logger.debug("Unexpected Exception!", e);
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public RpcConnection accept() throws IOException {
    Socket socket = serverSocket.accept();
    logger.info(String.format("Get connection from socket [%s].", socket));
    for (RpcServerListener listener : rpcServer.getServerListeners()) {
      listener.onAccept(new RpcServerAcceptEvent(endpoint, SimpleConnectionEndpoint.createEndpoint(socket.getInetAddress().getHostAddress(), socket.getPort())));
    }
    setupClientSocket(socket);
    RpcConnection connection = new BioRpcConnection(rpcServer, container, socket);
    return connection;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void interrupt() {
    stop = true;
  }
}