package com.elminster.easy.rpc.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.misc.Version;
import com.elminster.common.thread.ThreadUncatchedExceptionEvent;
import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.context.impl.SimpleConnectionEndpoint;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.impl.RpcEncodingFactoryBase;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.exception.StartContainerException;
import com.elminster.easy.rpc.server.container.exception.StopContainerException;
import com.elminster.easy.rpc.server.container.impl.ContainerFactoryImpl;
import com.elminster.easy.rpc.server.exception.ServerException;
import com.elminster.easy.rpc.server.listener.RpcServerListenEvent;
import com.elminster.easy.rpc.server.listener.RpcServerListener;
import com.elminster.easy.rpc.service.RpcService;

/**
 * The RPC Server.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcServerImpl implements RpcServer, Observer {

  /** the logger. */
  private static final Logger logger = LoggerFactory.getLogger(RpcServerImpl.class);

  private static final String DEFAULT_ENCODING_FACTORY_NAME = "default";

  /** the encoding factory. */
  protected RpcEncodingFactory encodingFactory;
  /** the RPC services. */
  protected Map<String, RpcService> rpcServices = new ConcurrentHashMap<>();
  /** version check? */
  private boolean versionCheck = false;
  /** use secure connection? */
  private boolean useSecureConnection = false;
  /** the network containers. */
  private List<Container> containers = new LinkedList<>();
  /** the server listeners. */
  private List<RpcServerListener> listeners = new ArrayList<>();
  /** the PRC context. */
  private final RpcContext context;

  public RpcServerImpl(RpcContext context) {
    this.context = context;
    setDefaultEncodingFactory();

  }

  private void setDefaultEncodingFactory() {
    RpcEncodingFactory defaultEncodingFactory = new RpcEncodingFactoryBase(DEFAULT_ENCODING_FACTORY_NAME);
    this.setEncodingFactory(defaultEncodingFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEncodingFactory(final RpcEncodingFactory encodingFactory) {
    Assert.notNull(encodingFactory);
    String encodingName = encodingFactory.getName();
    logger.info(String.format("Register encoding: [%s]", encodingName));
    this.encodingFactory = encodingFactory;
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
  public void listen(final int port) throws ServerException {
    ConnectionEndpoint endpoint = SimpleConnectionEndpoint.localhostConnectionEndpoint(port, this.useSecureConnection);
    logger.info(String.format("RPC server listen on endpoint: %s.", endpoint.toString()));
    for (RpcServerListener listener : listeners) {
      listener.beforeServe(new RpcServerListenEvent(endpoint));
    }

    try {
      final Container container = ContainerFactoryImpl.INSTANCE.getContainer(this, endpoint);
      this.containers.add(container);
      container.start();
    } catch (ObjectInstantiationExcption | StartContainerException e) {
      String message = String.format("Rpc server failed to listen on endpoint: %s.", endpoint);
      logger.error(message, e);
      throw new ServerException(message, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void shutdown(boolean force) throws ServerException {
    logger.info(String.format("Shutdown RPC server."));
    for (Container container : containers) {
      try {
        container.stop(force);
      } catch (StopContainerException e) {
        String message = String.format("Rpc server failed to shutdown on endpoint: %s.", container.getConnectionEndpoint());
        logger.error(message, e);
        throw new ServerException(message, e);
      }
    }
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
  public RpcEncodingFactory getEncodingFactory(Codec coreCodec) {
    RpcEncodingFactory rtn = null;
    if (null != this.encodingFactory) {
      rtn = encodingFactory.cloneEncodingFactory();
      rtn.setCodec(coreCodec);
    }
    return rtn;
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

  @Override
  public void addServerListener(RpcServerListener listener) {
    Assert.notNull(listener);
    this.listeners.add(listener);
  }

  @Override
  public void removeServerListener(RpcServerListener listener) {
    this.listeners.remove(listener);
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
  public List<RpcServerListener> getServerListeners() {
    return Collections.unmodifiableList(listeners);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getVersion() {
    return Version.getVersion(this.getClass());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getOpenConnectionCount() {
    int count = 0;
    for (Container container : this.containers) {
      count += container.getNumberOfOpenConnections();
    }
    return count;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void update(Observable o, Object arg) {
    if (arg instanceof ThreadUncatchedExceptionEvent) {
      ThreadUncatchedExceptionEvent event = (ThreadUncatchedExceptionEvent) arg;
      logger.error(event.toString());
    }
  }
}
