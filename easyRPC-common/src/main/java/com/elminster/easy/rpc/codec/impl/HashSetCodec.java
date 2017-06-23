package com.elminster.easy.rpc.codec.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

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
  public void encode(final OutputStream oStream, final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (null != value) {
        HashSet<?> set = (HashSet<?>) value;
        encodingFactory.writeObjectNullable(oStream, Integer.valueOf(set.size()));
        for (Object o : set) {
          encodingFactory.writeObjectNullable(oStream, o);
        }
      }
    } catch (RpcException k) {
      throw k;
    } catch (Exception e) {
      logger.error("HashSet encode:", e);
      throw new RpcException("Could not encode HashSet - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object decode(InputStream iStream, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      int size = ((Integer) encodingFactory.readObjectNullable(iStream)).intValue();

      HashSet<Object> hashSet = new HashSet<>(size);

      for (int i = 0; i < size; i++) {
        hashSet.add(encodingFactory.readObjectNullable(iStream));
      }
      return hashSet;
    } catch (RpcException k) {
      throw k;
    } catch (Exception e) {
      logger.error("HashSet decode:", e);
      throw new RpcException("Could not decode HashSet - " + e.getMessage());
    }
  }
}