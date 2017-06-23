package com.elminster.easy.rpc.server.container.impl;

import com.elminster.easy.rpc.connection.SocketFactory;
import com.elminster.easy.rpc.server.container.Container;

abstract public class ContainerBase implements Container {

  private final boolean useSecure;
  
  private SocketFactory socketFactory;
  
  public ContainerBase() {
    this(false);
  }
  
  public ContainerBase(boolean useSecure) {
    this.useSecure = useSecure;
  }

  public boolean isUseSecure() {
    return useSecure;
  }

}
