package com.elminster.easy.rpc.client.context.impl;

import java.net.InetAddress;

import com.elminster.easy.rpc.client.context.InvokerContext;
import com.elminster.easy.rpc.context.impl.InvokeContextImpl;

/**
 * The invoker context.
 * 
 * @author jinggu
 * @version 1.0
 */
public class InvokerContextImpl extends InvokeContextImpl implements InvokerContext {

  public InvokerContextImpl(InetAddress InvokeeHost, InetAddress clientHost, String InvokeeVersion, int InvokeePort, int clientPort, String clientVersion) {
    super(InvokeeHost, clientHost, InvokeeVersion, InvokeePort, clientPort, clientVersion);
  }

  public static class InvokerContextImplBuilder {
    private InetAddress InvokeeHost;
    private InetAddress clientHost;
    private String InvokeeVersion;
    private int InvokeePort;
    private int clientPort;
    private String clientVersion;

    public InvokerContextImplBuilder withServerHost(InetAddress InvokeeHost) {
      this.InvokeeHost = InvokeeHost;
      return this;
    }

    public InvokerContextImplBuilder withClientHost(InetAddress clientHost) {
      this.clientHost = clientHost;
      return this;
    }

    public InvokerContextImplBuilder withServerVersion(String InvokeeVersion) {
      this.InvokeeVersion = InvokeeVersion;
      return this;
    }

    public InvokerContextImplBuilder withServerPort(int InvokeePort) {
      this.InvokeePort = InvokeePort;
      return this;
    }

    public InvokerContextImplBuilder withClientPort(int clientPort) {
      this.clientPort = clientPort;
      return this;
    }

    public InvokerContextImplBuilder withClientVersion(String clientVersion) {
      this.clientVersion = clientVersion;
      return this;
    }

    public InvokerContextImpl build() {
      return new InvokerContextImpl(InvokeeHost, clientHost, InvokeeVersion, InvokeePort, clientPort, clientVersion);
    }
  }

  public static InvokerContextImplBuilder invokeContextImpl() {
    return new InvokerContextImplBuilder();
  }
}
