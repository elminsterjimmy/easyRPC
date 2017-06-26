package com.elminster.easy.rpc.codec.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

/**
 * The String Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class StringCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(StringCodec.class);
  
  /**
   * {@inheritDoc}
   */
  public Object decode(final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      return encodingFactory.readStringNullable();
    } catch (Exception e) {
      logger.error("String decode:", e);
      throw new RpcException("Could not decode String - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      encodingFactory.writeStringNullable((String) value);
    } catch (Exception e) {
      logger.error("String encode:", e);
      throw new RpcException("Could not encode String - " + e.getMessage());
    }
  }
}