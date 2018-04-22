package com.elminster.easy.rpc.codec.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.CodecException;

/**
 * Boolean Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class BooleanCodec implements RpcCodec {
  
  private static Logger logger = LoggerFactory.getLogger(BooleanCodec.class);

  /**
   * {@inheritDoc}
   */
  public Object decode(final RpcEncodingFactory encodingFactory) throws CodecException {
    try {
      byte value = encodingFactory.readInt8();
      if (1 == value) {
        return Boolean.TRUE;
      }
      return Boolean.FALSE;
    } catch (Exception e) {
      logger.error("Boolean decode:", e);
      throw new CodecException("Could not decode Boolean - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws CodecException {
    try {
      if (null != value) {
        if (((Boolean) value).booleanValue()) {
          encodingFactory.writeInt8((byte)1);
        } else {
          encodingFactory.writeInt8((byte) 0);
        }
      }
    } catch (Exception e) {
      logger.error("Boolean encode:", e);
      throw new CodecException("Could not encode Boolean - " + e.getMessage());
    }
  }
}
