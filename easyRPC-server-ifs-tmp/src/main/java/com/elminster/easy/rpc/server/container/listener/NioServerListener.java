package com.elminster.easy.rpc.server.container.listener;

import java.nio.channels.Selector;

public interface NioServerListener extends ServerListener {

  public void registerSelector(Selector selector);
}
