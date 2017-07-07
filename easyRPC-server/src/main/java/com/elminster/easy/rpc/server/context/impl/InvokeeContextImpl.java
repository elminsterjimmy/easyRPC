package com.elminster.easy.rpc.server.context.impl;

import java.net.InetAddress;

import com.elminster.easy.rpc.context.impl.InvokeContextImpl;
import com.elminster.easy.rpc.server.context.InvokeeContext;

public class InvokeeContextImpl extends InvokeContextImpl implements InvokeeContext {

  public InvokeeContextImpl(InetAddress InvokeeHost, InetAddress clientHost, String InvokeeVersion, int InvokeePort, int clientPort, String clientVersion) {
    super(InvokeeHost, clientHost, InvokeeVersion, InvokeePort, clientPort, clientVersion);
  }

  public static class InvokeeContextImplBuilder {
    private InetAddress InvokeeHost;
    private InetAddress clientHost;
    private String InvokeeVersion;
    private int InvokeePort;
    private int clientPort;
    private String clientVersion;

    public InvokeeContextImplBuilder withServerHost(InetAddress InvokeeHost) {
      this.InvokeeHost = InvokeeHost;
      return this;
    }

    public InvokeeContextImplBuilder withClientHost(InetAddress clientHost) {
      this.clientHost = clientHost;
      return this;
    }

    public InvokeeContextImplBuilder withServerVersion(String InvokeeVersion) {
      this.InvokeeVersion = InvokeeVersion;
      return this;
    }

    public InvokeeContextImplBuilder withServerPort(int InvokeePort) {
      this.InvokeePort = InvokeePort;
      return this;
    }

    public InvokeeContextImplBuilder withClientPort(int clientPort) {
      this.clientPort = clientPort;
      return this;
    }

    public InvokeeContextImplBuilder withClientVersion(String clientVersion) {
      this.clientVersion = clientVersion;
      return this;
    }

    public InvokeeContextImpl build() {
      return new InvokeeContextImpl(InvokeeHost, clientHost, InvokeeVersion, InvokeePort, clientPort, clientVersion);
    }
  }

  public static InvokeeContextImplBuilder invokeContextImpl() {
    return new InvokeeContextImplBuilder();
  }

}