package com.elminster.easy.rpc.server.impl;

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

  @Override
  public void listen(int port) throws RpcException {
    // TODO Auto-generated method stub
    

  }

  @Override
  public void shutdown(boolean force) {
    // TODO Auto-generated method stub

  }

  @Override
  public int getNumberOfOpenConnections() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public ThreadPoolExecutor getAsyncWorkerThreadPool() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isVersionCheck() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setVersionCheck(boolean versionCheck) {
    // TODO Auto-generated method stub

  }

  @Override
  public RpcEncodingFactory getEncodingFactory(String encodingName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeOpenConnection(RpcConnection connection) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addOpenConnection(RpcConnection connection) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isSecureConnection() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setUseSecureConnection(boolean useSecure) {
    // TODO Auto-generated method stub

  }

}
