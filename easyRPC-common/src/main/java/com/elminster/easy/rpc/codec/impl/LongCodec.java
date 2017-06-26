package com.elminster.easy.rpc.codec.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

public class LongCodec implements RpcCodec {

  private static Logger logger = LoggerFactory.getLogger(LongCodec.class);

  /**
   * {@inheritDoc}
   */
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (null != value) {
        encodingFactory.writeInt64(((Long) value).longValue());
      }
    } catch (Exception e) {
      logger.error("Long encode:", e);
      throw new RpcException("Could not encode Long - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public Object decode(final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      long value = encodingFactory.readInt64();
      return Long.valueOf(value);
    } catch (Exception e) {
      logger.error("Long decode:", e);
      throw new RpcException("Could not decode Long - " + e.getMessage());
    }
  }
}
