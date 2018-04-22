package com.elminster.easy.rpc.codec.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.CodecException;

/**
 * Map Codec.
 * Using HashMap as the default implementation.
 * 
 * @author jinggu
 * @version 1.0
 */
public class HashMapCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(HashMapCodec.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws CodecException {
    try {
      if (null != value) {
        Map<?, ?> map = (Map<?, ?>) value;
        
        encodingFactory.writeObjectNullable(Integer.valueOf(map.size()));
        for (Map.Entry<?, ?> e : map.entrySet()) {
          encodingFactory.writeObjectNullable(e.getKey());
          encodingFactory.writeObjectNullable(e.getValue());
        }
      }
    } catch (CodecException k) {
      throw k;
    } catch (Exception e) {
      logger.error("HashMap encode:", e);
      throw new CodecException("Could not encode HashMap - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object decode(final RpcEncodingFactory encodingFactory) throws CodecException {
    try {
      int size = ((Integer) encodingFactory.readObjectNullable()).intValue();
      Map<Object, Object> map = new HashMap<>(size);
      while (size > 0) {
        size--;
        Object curKey = encodingFactory.readObjectNullable();
        Object curValue = encodingFactory.readObjectNullable();
        map.put(curKey, curValue);
      }
      return map;
    } catch (CodecException k) {
      throw k;
    } catch (Exception e) {
      logger.error("HashMap decode:", e);
      throw new CodecException("Could not decode HashMap - " + e.getMessage());
    }
  }
}
