package com.elminster.easy.rpc.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.elminster.easy.rpc.context.ConnectionEndpoint;

/**
 * The Socket Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface SocketFactory {

  /**
   * Create a server socket on the port.
   * 
   * @param port
   *          the port
   * @param useSecure
   *          use secure connection?
   * @return a server socket
   * @throws IOException
   *           on error
   */
  public ServerSocket createServerSocket(int port, boolean useSecure) throws IOException;

  /**
   * Create a client socket.
   * 
   * @param connectionEndpoint
   *          the connection endpoint
   * @return a client socket
   * @throws IOException
   *           on error
   */
  public Socket createClientSocket(ConnectionEndpoint connectionEndpoint) throws IOException;
}