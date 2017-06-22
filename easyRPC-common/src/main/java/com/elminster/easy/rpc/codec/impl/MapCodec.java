package com.elminster.easy.rpc.codec.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

/**
 * Map Codec.
 * Using HashMap as the default implementation.
 * 
 * @author jinggu
 * @version 1.0
 */
public class MapCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(MapCodec.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public void encode(OutputStream oStream, Object value, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (null != value) {
        Map<?, ?> map = (Map<?, ?>) value;
        
        encodingFactory.writeObjectNullable(oStream, Integer.valueOf(map.size()));
        for (Map.Entry<?, ?> e : map.entrySet()) {
          encodingFactory.writeObjectNullable(oStream, e.getKey());
          encodingFactory.writeObjectNullable(oStream, e.getValue());
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
  public Object decode(InputStream iStream, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      int size = ((Integer) encodingFactory.readObjectNullable(iStream)).intValue();
      Map<Object, Object> map = new HashMap<>(size);
      while (size > 0) {
        size--;
        Object curKey = encodingFactory.readObjectNullable(iStream);
        Object curValue = encodingFactory.readObjectNullable(iStream);
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
