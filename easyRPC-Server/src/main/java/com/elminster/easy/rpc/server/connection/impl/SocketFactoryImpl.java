package com.elminster.easy.rpc.server.connection.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.exception.ConnectionException;

/**
 * The Socket Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public class SocketFactoryImpl implements SocketFactory {

  /** the logger. */
  private static final Logger logger = LoggerFactory.getLogger(SocketFactoryImpl.class);
  private static final String DEFAULT_SOCKET_TYPE = "TLSv1.2";
  private static final String KEY_STROE = "rpcSecure.keystore";
  private static final char[] SECURE_PASSWORD = "3|m1n573r.j1mmy46m41|.c0m".toCharArray();

  /** the server socket factory. */
  private static ServerSocketFactory serverSocketFactory;
  /** the client socket factory. */
  private static javax.net.SocketFactory clientSocketFactory;
  /** the SSL init throwable. */
  private static Throwable sslInitThrowable;

  /**
   * init the SSL socket factories.
   */
  static {
    try {
      ClassLoader classLoader = SocketFactoryImpl.class.getClassLoader();
      SSLContext srvContext = SSLContext.getInstance(DEFAULT_SOCKET_TYPE);
      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      KeyStore sks = KeyStore.getInstance("JKS");
      sks.load(classLoader.getResourceAsStream(KEY_STROE), SECURE_PASSWORD);
      kmf.init(sks, SECURE_PASSWORD);
      srvContext.init(kmf.getKeyManagers(), null, null);
      serverSocketFactory = srvContext.getServerSocketFactory();

      SSLContext clnContext = SSLContext.getInstance(DEFAULT_SOCKET_TYPE);
      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
      KeyStore cks = KeyStore.getInstance("JKS");
      cks.load(classLoader.getResourceAsStream(KEY_STROE), SECURE_PASSWORD);
      tmf.init(cks);
      clnContext.init(null, tmf.getTrustManagers(), null);
      clientSocketFactory = clnContext.getSocketFactory();
    } catch (Throwable t) {
      logger.error("SSL socket factory creation error: " + t.getMessage(), t);
      sslInitThrowable = t;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ServerSocket createServerSocket(int port, boolean useSecure) throws IOException {
    ServerSocket serverSocket;
    if (useSecure) {
      if (null == serverSocketFactory) {
        throw new ConnectionException("Unable to create SSL socket factory.", sslInitThrowable);
      }
      serverSocket = serverSocketFactory.createServerSocket(port);
    } else {
      serverSocket = ServerSocketFactory.getDefault().createServerSocket(port);
    }
    return serverSocket;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createClientSocket(ConnectionEndpoint connectionEndpoint) throws IOException {
    vaildateEndpoint(connectionEndpoint);
    Socket socket;
    if (connectionEndpoint.useSecureSocket()) {
      if (null == clientSocketFactory) {
        throw new ConnectionException("Unable to create SSL socket factory.", sslInitThrowable);
      }
      socket = clientSocketFactory.createSocket(connectionEndpoint.getHost(), connectionEndpoint.getPort());
    } else {
      socket = javax.net.SocketFactory.getDefault().createSocket(connectionEndpoint.getHost(), connectionEndpoint.getPort());
    }
    return socket;
  }

  /**
   * Validate the connection endpoint.
   * 
   * @param connectionEndpoint
   *          the connection endpoint
   */
  private void vaildateEndpoint(ConnectionEndpoint connectionEndpoint) {
    Assert.notNull(connectionEndpoint);
    Assert.notNull(connectionEndpoint.getHost());
    Assert.notNull(connectionEndpoint.getPort());
    Assert.notNull(connectionEndpoint.useSecureSocket());
  }

}
