package com.elminster.easy.rpc.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.misc.Version;
import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.client.container.Container;
import com.elminster.easy.rpc.client.container.exception.ContainerConnectionException;
import com.elminster.easy.rpc.client.container.impl.ContainerFactoryImpl;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.exception.ConnectionException;

/**
 * The Rpc client.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcClientImpl implements RpcClient {
  
  private static final Logger logger = LoggerFactory.getLogger(RpcClientImpl.class);
  
  private final ConnectionEndpoint endpoint;
  private final RpcEncodingFactory encodingFactory;
  private final RpcContext context;
  private Container container;
  
  public RpcClientImpl(ConnectionEndpoint endpoint, RpcEncodingFactory encodingFactory, RpcContext context) {
    Assert.notNull(endpoint);
    Assert.notNull(context);
    Assert.notNull(encodingFactory);
    this.endpoint = endpoint;
    this.encodingFactory = encodingFactory;
    this.context = context;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConnectionEndpoint getConnectionEndpoint() {
    return endpoint;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcEncodingFactory getEncodingFactory() {
    return encodingFactory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSecureConnection() {
    return endpoint.useSecureSocket().booleanValue();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void connect() throws ConnectionException {
    if (null == container) {
      try {
        initContainer();
      } catch (ObjectInstantiationExcption e) {
        String msg = "Fail to instantiate container!";
        logger.error(msg, e);
        throw new ConnectionException(msg, e);
      }
    }
    if (!isConnected()) {
      try {
        container.connect();
      } catch (ContainerConnectionException e) {
        String msg = String.format("Cannot connect to RPC server [%s].", endpoint);
        logger.error(msg, e);
        throw new ConnectionException(msg, e);
      }
    }
  }

  private void initContainer() throws ObjectInstantiationExcption {
    container = ContainerFactoryImpl.INSTANCE.getContainer(this, endpoint);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void disconnect() {
    if (isConnected()) {
      container.disconnect();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isConnected() {
    Assert.notNull(container, "Rpc client still not connect to any server!");
    return container.isConnected();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcContext getRpcContext() {
    return context;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object invokeService(String serviceName, String methodName, Object[] args) throws Throwable {
    if (isConnected()) {
      return container.getProcessor().invokeService(serviceName, methodName, args);
    } else {
      String msg = "Rpc client still not connect to any server!";
      logger.warn(msg);
      throw new IllegalStateException(msg);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getVersion() {
    return Version.getVersion(this.getClass());
  }
}
