package com.elminster.easy.rpc.context.impl;

import java.net.InetAddress;

import com.elminster.easy.rpc.context.InvokeContext;

public class InvokeContextImpl implements InvokeContext {

  private InetAddress serverHost;
  private InetAddress clientHost;
  private String serverVersion;
  private int serverPort;
  private int clientPort;
  private String clientVersion;
  
  public InvokeContextImpl(InetAddress InvokeeHost, InetAddress clientHost, String InvokeeVersion, int InvokeePort, int clientPort, String clientVersion) {
    super();
    this.serverHost = InvokeeHost;
    this.clientHost = clientHost;
    this.serverVersion = InvokeeVersion;
    this.serverPort = InvokeePort;
    this.clientPort = clientPort;
    this.clientVersion = clientVersion;
  }


  public InetAddress getInvokeeHost() {
    return serverHost;
  }

  public void setInvokeeHost(InetAddress InvokeeHost) {
    this.serverHost = InvokeeHost;
  }

  public String getInvokeeVersion() {
    return serverVersion;
  }

  public void setInvokeeVersion(String InvokeeVersion) {
    this.serverVersion = InvokeeVersion;
  }

  public int getInvokeePort() {
    return serverPort;
  }

  public void setInvokeePort(int InvokeePort) {
    this.serverPort = InvokeePort;
  }


  @Override
  public InetAddress getInvokerHost() {
    return this.clientHost;
  }
  
  public void setInvokerHost(InetAddress invokerHost) {
    this.clientHost = invokerHost;
  }

  @Override
  public int getInvokerPort() {
    return this.clientPort;
  }
  
  public void setInvokerPort(int clientPort) {
    this.clientPort = clientPort;
  }
  
  @Override
  public String getInvokerVersion() {
    return this.clientVersion;
  }
  
  public void setInvokerVersion(String clientVersion) {
    this.clientVersion = clientVersion;
  }

  @Override
  public String toString() {
    return String.format("Server=[ host=%s, port=%d, version=%s ] | Clietn=[ host=%s, port=%d, version=%s ]", serverHost, serverPort, serverVersion, clientHost, clientPort,
        clientVersion);
  }

}
