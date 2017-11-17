package com.elminster.easy.rpc.codec.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

public class IntegerCodec implements RpcCodec {

  private static Logger logger = LoggerFactory.getLogger(IntegerCodec.class);
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (null != value) {
        encodingFactory.writeInt32(((Integer) value).intValue());
      }
    } catch (Exception e) {
      logger.error("Integer encode:", e);
      throw new RpcException("Could not encode Integer - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object decode(final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      int value = encodingFactory.readInt32();
      return Integer.valueOf(value);
    } catch (Exception e) {
      logger.error("Integer decode:", e);
      throw new RpcException("Could not decode Integer - " + e.getMessage());
    }
  }
}