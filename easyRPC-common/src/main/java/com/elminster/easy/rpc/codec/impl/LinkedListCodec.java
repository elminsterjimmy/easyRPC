package com.elminster.easy.rpc.codec.impl;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

/**
 * List Codec.
 * Using LinkedList as default implementation.
 * 
 * @author jinggu
 * @version 1.0
 */
public class LinkedListCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(LinkedListCodec.class);

  /**
   * {@inheritDoc}
   */
  public Object decode(final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      int size = ((Integer) encodingFactory.readObjectNullable()).intValue();
      List<Object> list = new LinkedList<>();
      for (int i = 0; i < size; i++) {
        list.add(encodingFactory.readObjectNullable());
      }
      return list;
    } catch (RpcException k) {
      throw k;
    } catch (Exception e) {
      String message = "Could not decode LinkedList - " + e;
      logger.error(message, e);
      throw new RpcException(message);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (null != value) {
        List<?> list = (List<?>) value;
        
        encodingFactory.writeObjectNullable(Integer.valueOf(list.size()));
        for (Object o : list) {
          encodingFactory.writeObjectNullable(o);
        }
      }
    } catch (RpcException k) {
      throw k;
    } catch (Exception e) {
      String message = "Could not encode LinkedList - " + e;
      logger.error(message, e);
      throw new RpcException(message);
    }
  }
}
