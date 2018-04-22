package com.elminster.easy.rpc.codec;

import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.CodecException;

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
   * @param value
   *          the value
   * @param encodingFactory
   *          the encode factory
   * @throws CodecException
   *           on error
   */
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws CodecException;

  /**
   * Decode to the value.
   * 
   * @param encodingFactory
   *          the encode factory
   * @return the value
   * @throws CodecException
   *           on error
   */
  public Object decode(final RpcEncodingFactory encodingFactory) throws CodecException;
}
