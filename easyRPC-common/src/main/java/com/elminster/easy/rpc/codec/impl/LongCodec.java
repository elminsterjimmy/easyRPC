package com.elminster.easy.rpc.codec.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.CodecException;

public class LongCodec implements RpcCodec {

  private static Logger logger = LoggerFactory.getLogger(LongCodec.class);

  /**
   * {@inheritDoc}
   */
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws CodecException {
    try {
      if (null != value) {
        encodingFactory.writeInt64(((Long) value).longValue());
      }
    } catch (Exception e) {
      logger.error("Long encode:", e);
      throw new CodecException("Could not encode Long - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public Object decode(final RpcEncodingFactory encodingFactory) throws CodecException {
    try {
      long value = encodingFactory.readInt64();
      return Long.valueOf(value);
    } catch (Exception e) {
      logger.error("Long decode:", e);
      throw new CodecException("Could not decode Long - " + e.getMessage());
    }
  }
}
