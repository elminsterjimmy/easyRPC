package com.elminster.easy.rpc.codec.impl;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.CodecException;

public class TreeMapCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(TreeMapCodec.class);

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
      Map<Object, Object> map = new TreeMap<>();
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
