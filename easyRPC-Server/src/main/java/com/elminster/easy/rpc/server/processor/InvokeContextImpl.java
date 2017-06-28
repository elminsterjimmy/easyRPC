package com.elminster.easy.rpc.server.processor;

import java.net.InetAddress;

public class InvokeContextImpl implements InvokeContext {

  private InetAddress serverHost;
  private InetAddress clientHost;
  private String serverVersion;
  private int serverPort;
  private int clientPort;
  private String clientVersion;

  public InetAddress getServerHost() {
    return serverHost;
  }

  public void setServerHost(InetAddress serverHost) {
    this.serverHost = serverHost;
  }

  public InetAddress getClientHost() {
    return clientHost;
  }

  public void setClientHost(InetAddress clientHost) {
    this.clientHost = clientHost;
  }

  public String getServerVersion() {
    return serverVersion;
  }

  public void setServerVersion(String serverVersion) {
    this.serverVersion = serverVersion;
  }

  public int getServerPort() {
    return serverPort;
  }

  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  public int getClientPort() {
    return clientPort;
  }

  public void setClientPort(int clientPort) {
    this.clientPort = clientPort;
  }

  public String getClientVersion() {
    return clientVersion;
  }

  public void setClientVersion(String clientVersion) {
    this.clientVersion = clientVersion;
  }

  public InvokeContextImpl(InetAddress serverHost, InetAddress clientHost, String serverVersion, int serverPort, int clientPort, String clientVersion) {
    super();
    this.serverHost = serverHost;
    this.clientHost = clientHost;
    this.serverVersion = serverVersion;
    this.serverPort = serverPort;
    this.clientPort = clientPort;
    this.clientVersion = clientVersion;
  }

  public static class InvokeContextImplBuilder {
    private InetAddress serverHost;
    private InetAddress clientHost;
    private String serverVersion;
    private int serverPort;
    private int clientPort;
    private String clientVersion;

    public InvokeContextImplBuilder withServerHost(InetAddress serverHost) {
      this.serverHost = serverHost;
      return this;
    }

    public InvokeContextImplBuilder withClientHost(InetAddress clientHost) {
      this.clientHost = clientHost;
      return this;
    }

    public InvokeContextImplBuilder withServerVersion(String serverVersion) {
      this.serverVersion = serverVersion;
      return this;
    }

    public InvokeContextImplBuilder withServerPort(int serverPort) {
      this.serverPort = serverPort;
      return this;
    }

    public InvokeContextImplBuilder withClientPort(int clientPort) {
      this.clientPort = clientPort;
      return this;
    }

    public InvokeContextImplBuilder withClientVersion(String clientVersion) {
      this.clientVersion = clientVersion;
      return this;
    }

    public InvokeContextImpl build() {
      return new InvokeContextImpl(serverHost, clientHost, serverVersion, serverPort, clientPort, clientVersion);
    }
  }

  public static InvokeContextImplBuilder invokeContextImpl() {
    return new InvokeContextImplBuilder();
  }

  @Override
  public InetAddress getInvokerHost() {
    return this.clientHost;
  }

  @Override
  public int getInvokerPort() {
    return this.clientPort;
  }

  @Override
  public String getInvokerVersion() {
    return this.clientVersion;
  }

  @Override
  public String toString() {
    return String.format("Server=[ host=%s, port=%d, version=%s ] | Clietn=[ host=%s, port=%d, version=%s ]", serverHost, serverPort, serverVersion, clientHost, clientPort,
        clientVersion);
  }

}
