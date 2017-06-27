package com.elminster.easy.rpc.server.container.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.exception.StartContainerException;
import com.elminster.easy.rpc.server.container.exception.StopContainerException;

/**
 * The Container Base.
 * 
 * @author jinggu
 * @version 1.0
 */
abstract public class ContainerBase implements Container {

  protected final RpcServer rpcServer;
  protected final ConnectionEndpoint endpoint;
  private volatile boolean isServing = false;
  /** the lock. */
  protected Lock lock = new ReentrantLock();
  /** the worker thread pool. */
  private ThreadPoolExecutor workerThreadPool;
  /** the open connections. */
  private final List<RpcConnection> openConnections = new LinkedList<>();
  

  public ContainerBase(RpcServer rpcServer, ConnectionEndpoint endpoint) {
    this.rpcServer = rpcServer;
    this.endpoint = endpoint;
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
    startWorkerThreads();
    try {
      serve();
    } catch (Exception e) {
      throw new StartContainerException(e);
    }
  }

  abstract protected void startWorkerThreads();
  
  abstract protected void serve() throws Exception;
  
  protected void setServing(boolean isServing) {
    this.isServing = isServing;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop(boolean closeConnections) throws StopContainerException {
    try {
      stopServe();
      
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
  protected void removeOpenConnection(RpcConnection connection) {
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
  protected void addOpenConnection(RpcConnection connection) {
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
    return workerThreadPool;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConnectionEndpoint getConnectionEndpoint() {
    return this.endpoint;
  }
}
