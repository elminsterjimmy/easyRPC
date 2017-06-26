package com.elminster.easy.rpc.server.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.exception.ServerException;
import com.elminster.easy.rpc.service.RpcService;

/**
 * The RPC Server.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcServerImpl implements RpcServer {

  /** the logger. */
  private static final Logger logger = LoggerFactory.getLogger(RpcServerImpl.class);

  /** the open connections. */
  private final List<RpcConnection> openConnections = new LinkedList<>();
  /** the encoding factories. */
  protected Map<String, RpcEncodingFactory> encodingFactories = new ConcurrentHashMap<>();
  /** the RPC services. */
  protected Map<String, RpcService> rpcServices = new ConcurrentHashMap<>();
  /** version check? */
  private boolean versionCheck = false;
  /** use secure connection? */
  private boolean useSecureConnection = false;
  /** the network containers. */
  private List<Container> containers = new LinkedList<>();
  /** the lock. */
  private Lock lock = new ReentrantLock();

  /**
   * {@inheritDoc}
   */
  @Override
  public void addEncodingFactory(final RpcEncodingFactory encodingFactory) {
    Assert.notNull(encodingFactory);
    String encodingName = encodingFactory.getEncodingName();
    logger.info(String.format("Register encoding: [%s]", encodingName));
    this.encodingFactories.put(encodingName, encodingFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addService(final RpcService rpcService) throws RpcException {
    Assert.notNull(rpcService);
    String serviceName = rpcService.getServiceName();
    logger.info(String.format("Register RPC service [%s].", rpcService));
    rpcServices.put(serviceName, rpcService);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcService getService(final String serviceName) throws RpcException {
    return rpcServices.get(serviceName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void listen(int port) throws ServerException {
    logger.info(String.format("RPC server listen on [%d]", port));
    Container container = null; // TODO
    container.start(port, useSecureConnection());
    this.containers.add(container);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void shutdown(boolean force) throws ServerException {
    logger.info(String.format("Shutdown RPC server."));
    for (Container container : containers) {
      container.stop();
    }
    if (force) {
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
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfOpenConnections() {
    return openConnections.size();
  }

  @Override
  public ThreadPoolExecutor getAsyncWorkerThreadPool() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isVersionCheck() {
    return versionCheck;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setVersionCheck(boolean versionCheck) {
    this.versionCheck = versionCheck;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcEncodingFactory getEncodingFactory(String encodingName) {
    return encodingFactories.get(encodingName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
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
  @Override
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
  public boolean useSecureConnection() {
    return useSecureConnection;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setUseSecureConnection(boolean useSecure) {
    this.useSecureConnection = useSecure;
  }
}
