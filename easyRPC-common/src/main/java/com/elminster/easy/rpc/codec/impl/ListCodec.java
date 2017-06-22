package com.elminster.easy.rpc.codec.impl;

import java.io.InputStream;
import java.io.OutputStream;
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
public class ListCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(ListCodec.class);

  /**
   * {@inheritDoc}
   */
  public Object decode(InputStream iStream, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      int size = ((Integer) encodingFactory.readObjectNullable(iStream)).intValue();
      List<Object> list = new LinkedList<>();
      for (int i = 0; i < size; i++) {
        list.add(encodingFactory.readObjectNullable(iStream));
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
  public void encode(OutputStream oStream, Object value, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (null != value) {
        List<?> list = (List<?>) value;
        
        encodingFactory.writeObjectNullable(oStream, Integer.valueOf(list.size()));
        for (Object o : list) {
          encodingFactory.writeObjectNullable(oStream, o);
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
