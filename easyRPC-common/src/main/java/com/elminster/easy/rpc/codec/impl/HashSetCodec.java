package com.elminster.easy.rpc.codec.impl;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.CodecException;

/**
 * HashSet Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class HashSetCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(HashSetCodec.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws CodecException {
    try {
      if (null != value) {
        HashSet<?> set = (HashSet<?>) value;
        encodingFactory.writeObjectNullable(Integer.valueOf(set.size()));
        for (Object o : set) {
          encodingFactory.writeObjectNullable(o);
        }
      }
    } catch (CodecException k) {
      throw k;
    } catch (Exception e) {
      logger.error("HashSet encode:", e);
      throw new CodecException("Could not encode HashSet - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object decode(RpcEncodingFactory encodingFactory) throws CodecException {
    try {
      int size = ((Integer) encodingFactory.readObjectNullable()).intValue();

      HashSet<Object> hashSet = new HashSet<>(size);

      for (int i = 0; i < size; i++) {
        hashSet.add(encodingFactory.readObjectNullable());
      }
      return hashSet;
    } catch (CodecException k) {
      throw k;
    } catch (Exception e) {
      logger.error("HashSet decode:", e);
      throw new CodecException("Could not decode HashSet - " + e.getMessage());
    }
  }
}