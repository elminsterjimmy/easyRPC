package com.elminster.easy.rpc.server.container.listener;

import java.io.IOException;

import com.elminster.easy.rpc.connection.RpcConnection;

/**
 * The server listener interface.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface ServerListener {

  /**
   * Server listen.
   * 
   * @throws IOException
   *           on error
   */
  public void listen() throws IOException;

  /**
   * Server accepts connection.
   * 
   * @return the accepted connection
   * @throws IOException
   *           on error
   */
  public RpcConnection accept() throws IOException;

  /**
   * Close the server.
   * 
   * @throws IOException
   *           on error
   */
  public void close() throws IOException;

  /**
   * Interrupt the server.
   */
  public void interrupt();

}
