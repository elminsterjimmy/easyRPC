package com.elminster.easy.rpc.codec.impl;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

public class TreeMapCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(TreeMapCodec.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (null != value) {
        Map<?, ?> map = (Map<?, ?>) value;
        
        encodingFactory.writeObjectNullable(Integer.valueOf(map.size()));
        for (Map.Entry<?, ?> e : map.entrySet()) {
          encodingFactory.writeObjectNullable(e.getKey());
          encodingFactory.writeObjectNullable(e.getValue());
        }
      }
    } catch (RpcException k) {
      throw k;
    } catch (Exception e) {
      logger.error("HashMap encode:", e);
      throw new RpcException("Could not encode HashMap - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object decode(final RpcEncodingFactory encodingFactory) throws RpcException {
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
    } catch (RpcException k) {
      throw k;
    } catch (Exception e) {
      logger.error("HashMap decode:", e);
      throw new RpcException("Could not decode HashMap - " + e.getMessage());
    }
  }


}