package com.elminster.easy.rpc.codec.impl;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

/**
 * The Timestamp Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class TimestampCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(TimestampCodec.class);
  
  /**
   * {@inheritDoc}
   */
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (value != null) {
        encodingFactory.writeInt64(((Timestamp) value).getTime());
      }
    } catch (Exception e) {
      logger.error("Timestamp encode:", e);
      throw new RpcException("Could not encode Timestamp - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public Object decode(final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      return new Timestamp(encodingFactory.readInt64());
    } catch (Exception e) {
      logger.error("Timestamp decode:", e);
      throw new RpcException("Could not decode Timestamp - " + e.getMessage());
    }
  }
}