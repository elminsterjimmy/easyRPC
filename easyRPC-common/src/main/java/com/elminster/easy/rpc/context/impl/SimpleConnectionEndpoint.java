package com.elminster.easy.rpc.context.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.elminster.easy.rpc.context.ConnectionEndpoint;

/**
 * The Simple Connection Endpoint.
 * 
 * @author jinggu
 * @version 1.0
 */
public class SimpleConnectionEndpoint implements ConnectionEndpoint {

  private final String host;
  private final int port;
  private final boolean useSecure;

  public SimpleConnectionEndpoint(String host, int port, boolean useSecure) {
    this.host = host;
    this.port = port;
    this.useSecure = useSecure;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public Integer getPort() {
    return port;
  }

  @Override
  public Boolean useSecureSocket() {
    return useSecure;
  }

  public String toString() {
    return String.format("host on [%s:%d] with secure [%b]", host, port, useSecure);
  }
  
  /**
   * Create a new localhost Connection Endpoint without secure connection.
   * 
   * @param port
   *          the port
   * @param useSecure
   *          use secure conneciton?
   * @return the new localhost connection endpoint without secure connection
   */
  public static ConnectionEndpoint localhostConnectionEndpoint(int port) {
    return localhostConnectionEndpoint(port, false);
  }

  /**
   * Create a new localhost Connection Endpoint.
   * 
   * @param port
   *          the port
   * @param useSecure
   *          use secure conneciton?
   * @return the new localhost connection endpoint
   */
  public static ConnectionEndpoint localhostConnectionEndpoint(int port, boolean useSecure) {
    String hostname = "localhost";
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
    }
    return new SimpleConnectionEndpoint(hostname, port, useSecure);
  }

  /**
   * Create a new Connection Endpoint without secure connection.
   * 
   * @param host
   *          the hostname
   * @param port
   *          the port
   * @return the new connection endpoint without secure connection
   */
  public static ConnectionEndpoint createEndpoint(String host, int port) {
    return createEndpoint(host, port, false);
  }

  /**
   * Create a new Connection Endpoint.
   * 
   * @param host
   *          the hostname
   * @param port
   *          the port
   * @param useSecure
   *          use secure conneciton?
   * @return the new connection endpoint
   */
  public static ConnectionEndpoint createEndpoint(String host, int port, boolean useSecure) {
    return new SimpleConnectionEndpoint(host, port, useSecure);
  }
}
