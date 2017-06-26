package com.elminster.easy.rpc.server.container.impl;

import java.io.IOException;
import java.net.ServerSocket;

import javax.net.SocketFactory;

import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.exception.ServerException;

public class SocketContainer extends ContainerBase implements Container {
  

  /**
   * {@inheritDoc}  
   */
  @Override
  public void start(int port) throws ServerException {
    // TODO Auto-generated method stub
    socketFactory.createSocket();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() throws ServerException {
    // TODO Auto-generated method stub
    
  }
}
