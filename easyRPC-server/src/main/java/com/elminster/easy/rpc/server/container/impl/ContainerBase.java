package com.elminster.easy.rpc.server.container.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.threadpool.ThreadPool;
import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.exception.StartContainerException;
import com.elminster.easy.rpc.server.container.exception.StopContainerException;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;
import com.elminster.easy.rpc.server.processor.impl.RpcServiceProcessorDelegate;

/**
 * The Container Base.
 * 
 * @author jinggu
 * @version 1.0
 */
abstract public class ContainerBase implements Container {

  private static final Logger logger = LoggerFactory.getLogger(ContainerBase.class);

  protected final RpcServer rpcServer;
  protected final ConnectionEndpoint endpoint;

  private volatile boolean isServing = false;
  /** the lock. */
  protected Lock lock = new ReentrantLock();
  /** the worker thread pool. */
  private final ThreadPool workerThreadPool;
  /** the open connections. */
  private final List<RpcConnection> openConnections = new LinkedList<>();

  protected final RpcServiceProcessorDelegate serviceProcessor;

  public ContainerBase(RpcServer rpcServer, ConnectionEndpoint endpoint) {
    this.rpcServer = rpcServer;
    this.endpoint = endpoint;
    this.workerThreadPool = ThreadPool.createThreadPool(rpcServer.getContext().getWorkerThreadPoolConfiguration());
    serviceProcessor = new RpcServiceProcessorDelegate(rpcServer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isUseSecure() {
    return this.endpoint.useSecureSocket();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() throws StartContainerException {
    try {
      startWorkerThreads();
      serve();
    } catch (Exception e) {
      throw new StartContainerException(e);
    }
  }

  /**
   * Start the worker threads.
   * 
   * @throws Exception
   *           on error
   */
  abstract protected void startWorkerThreads() throws Exception;

  /**
   * Serve as service.
   * 
   * @throws Exception
   *           on error
   */
  abstract protected void serve() throws Exception;

  /**
   * Set isServing flag.
   * 
   * @param isServing
   *          is serving or not
   */
  protected void setServing(boolean isServing) {
    this.isServing = isServing;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop(boolean closeConnections) throws StopContainerException {
    logger.debug("Stop serve on endpoint [{}].", endpoint);
    try {
      stopServe();
      // release resources
      workerThreadPool.shutdown();

      if (closeConnections) {
        try {
          this.lock.lock();
          Iterator<RpcConnection> it = this.openConnections.iterator();
          while (it.hasNext()) {
            RpcConnection c = it.next();
            c.close();
            it.remove();
          }
        } finally {
          this.lock.unlock();
        }
      }
    } catch (Exception e) {
      throw new StopContainerException(e);
    } finally {
      isServing = false;
    }
  }

  /**
   * Stop serve the service.
   * 
   * @throws Exception
   *           on error
   */
  abstract protected void stopServe() throws Exception;

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isServing() {
    return isServing;
  }

  /**
   * {@inheritDoc}
   */
  public void removeOpenConnection(RpcConnection connection) {
    try {
      lock.lock();
      openConnections.remove(connection);
    } finally {
      lock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addOpenConnection(RpcConnection connection) {
    try {
      lock.lock();
      openConnections.add(connection);
    } finally {
      lock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfOpenConnections() {
    try {
      lock.lock();
      return openConnections.size();
    } finally {
      lock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ThreadPoolExecutor getAsyncWorkerThreadPool() {
    return workerThreadPool.getExecutor();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConnectionEndpoint getConnectionEndpoint() {
    return this.endpoint;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcServiceProcessor getServiceProcessor() {
    return serviceProcessor;
  }
}
