package com.elminster.easy.rpc.codec.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

/**
 * The Object Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ObjectCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(ObjectCodec.class);

  /**
   * {@inheritDoc}
   */
  public Object decode(final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      return encodingFactory.readObjectNullable();
    } catch (RpcException k) {
      throw k;
    } catch (Exception e) {
      String message = "Could not decode Object - " + e;
      logger.error(message, e);
      throw new RpcException(message);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      encodingFactory.writeObjectNullable(value);
    } catch (RpcException k) {
      throw k;
    } catch (Exception e) {
      String message = "Could not decode Object - " + e;
      logger.error(message, e);
      throw new RpcException(message);
    }
  }
}