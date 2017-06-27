package com.elminster.easy.rpc.context.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.elminster.easy.rpc.context.ConnectionEndpoint;

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

  public static ConnectionEndpoint localhostConnectionEndpoint(int port, boolean useSecure) {
    String hostname = "localhost";
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
    }
    return new SimpleConnectionEndpoint(hostname, port, useSecure);
  }
}
