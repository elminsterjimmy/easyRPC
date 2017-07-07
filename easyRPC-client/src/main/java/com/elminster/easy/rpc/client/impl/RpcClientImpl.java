package com.elminster.easy.rpc.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.misc.Version;
import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.client.connection.Connection;
import com.elminster.easy.rpc.client.container.Container;
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
  private Connection connection;
  private boolean stayConnection = false;
  private volatile boolean isConnected = false;

  public RpcClientImpl(ConnectionEndpoint endpoint, RpcEncodingFactory encodingFactory, RpcContext context, boolean stayConnection) {
    Assert.notNull(endpoint);
    Assert.notNull(context);
    Assert.notNull(encodingFactory);
    this.endpoint = endpoint;
    this.encodingFactory = encodingFactory;
    this.context = context;
    this.stayConnection = stayConnection;
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
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("Connect to Endpoint: [%s].", endpoint));
    }
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
        connection = container.connect();
      } catch (ConnectionException e) {
        String msg = String.format("Cannot connect to RPC server [%s].", endpoint);
        logger.error(msg, e);
        throw new ConnectionException(msg, e);
      }
    }
    isConnected = true;
  }

  private synchronized void initContainer() throws ObjectInstantiationExcption {
    if (null == container) {
      container = ContainerFactoryImpl.INSTANCE.getContainer(this, endpoint);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void disconnect() {
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("Disconnect from endpoint [%s]", endpoint));
    }
    if (isConnected()) {
      container.disconnect();
    }
    isConnected = false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isConnected() {
    return isConnected;
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
  public synchronized Object invokeService(RpcCall rpcCall) throws Throwable {
    if (rpcCall.isAsyncCall()) {
      // TODO always create new connection for async call
      if (null == container) {
        initContainer();
      }
      Connection asyncConn = null;
      try {
        asyncConn = container.connect();
        return null;
      } finally {
        if (null != asyncConn) {
          asyncConn.disconnect();
        }
      }
    } else {
      if (isConnected()) {
        try {
          return connection.invokeService(rpcCall);
        } finally {
          if (!stayConnection) {
            disconnect();
          }
        }
      } else {
        String msg = "Rpc client still not connect to any server!";
        logger.warn(msg);
        throw new IllegalStateException(msg);
      }
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
