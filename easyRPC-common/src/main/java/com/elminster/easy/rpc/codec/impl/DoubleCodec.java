package com.elminster.easy.rpc.codec.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.CodecException;

/**
 * Double Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public final class DoubleCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(DoubleCodec.class);
  
  /**
   * {@inheritDoc}
   */
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws CodecException {
    try {
      encodingFactory.writeDouble(((Double) value).doubleValue());
    } catch (Exception e) {
      logger.error("Double encode:", e);
      throw new CodecException("Could not encode Double - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public Object decode(final RpcEncodingFactory encodingFactory) throws CodecException {
    try {
      return new Double(encodingFactory.readDouble());
    } catch (Exception e) {
      logger.error("Double decode:", e);
      throw new CodecException("Could not decode Double - " + e.getMessage());
    }
  }
}
