package com.elminster.easy.rpc.protocol;

import java.io.IOException;

import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

/**
 * The protocol interface.
 * 
 * @author jinggu
 *
 */
public interface Protocol<T> {

  /**
   * Encode the message with specified encoding factory.
   * 
   * @param message
   *          the message
   * @param encodingFactory
   *          the encoding factory
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on error
   */
  public void encode(T message, RpcEncodingFactory encodingFactory) throws IOException, RpcException;

  /**
   * Decode the message from specified encoding factory.
   * 
   * @param encodingFactory
   *          the encoding factory
   * @return decoded message
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on error
   */
  public T decode(RpcEncodingFactory encodingFactory) throws IOException, RpcException;
}
