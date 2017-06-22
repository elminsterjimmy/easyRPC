package com.elminster.easy.rpc.codec;

import java.io.InputStream;
import java.io.OutputStream;

import com.elminster.easy.rpc.exception.RpcException;

/**
 * The RPC codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcCodec {

  /**
   * Encode the value.
   * 
   * @param oStream
   *          the output stream
   * @param value
   *          the value
   * @param encodingFactory
   *          the encode factory
   * @throws RpcException
   *           on error
   */
  public void encode(OutputStream oStream, Object value, RpcEncodingFactory encodingFactory) throws RpcException;

  /**
   * Decode to the value.
   * 
   * @param iStream
   *          the input stream
   * @param encodingFactory
   *          the encode factory
   * @return the value
   * @throws RpcException
   *           on error
   */
  public Object decode(InputStream iStream, RpcEncodingFactory encodingFactory) throws RpcException;
}
