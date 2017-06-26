package com.elminster.easy.rpc.codec.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

/**
 * Byte Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ByteCodec implements RpcCodec {
  
  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(ByteCodec.class);

  /**
   * {@inheritDoc}
   */
  public Object decode(final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      byte streamValue = encodingFactory.readInt8();
      return Byte.valueOf(streamValue);
    } catch (Exception e) {
      logger.error("Byte decode:", e);
      throw new RpcException("Could not decode Byte - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      encodingFactory.writeInt8(((Byte) value).byteValue());
    } catch (Exception e) {
      logger.error("Byte encode:", e);
      throw new RpcException("Could not encode Byte - " + e.getMessage());
    }
  }
}
