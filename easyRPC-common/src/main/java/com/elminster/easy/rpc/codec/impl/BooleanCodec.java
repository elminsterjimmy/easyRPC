package com.elminster.easy.rpc.codec.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

public class BooleanCodec implements RpcCodec {
  private static Logger log = LoggerFactory.getLogger(BooleanCodec.class);

  public Object decode(InputStream iStream, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      byte streamValue = CoreServiceRegistry.INSTANCE.getKisRpcUtil().readByte(iStream);
      if (1 == streamValue) {
        return Boolean.TRUE;
      }
      return Boolean.FALSE;
    } catch (Exception e) {
      log.error("Boolean decode:", e);
      throw new RpcException("Could not decode Boolean - " + e.getMessage());
    }
  }

  public void encode(OutputStream oStream, Object value, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (value == null) {
        return;
      }
      if (((Boolean) value).booleanValue()) {
        oStream.write(1);
      } else {
        oStream.write(0);
      }
    } catch (Exception e) {
      log.error("Boolean encode:", e);
      throw new RpcException("Could not encode Boolean - " + e.getMessage());
    }
  }

  public Object decode(InputStream iStream, Object codecData, RpcEncodingFactory encodingFactory) throws RpcException {
    return decode(iStream, encodingFactory);
  }

  public Object convertArray(Object value) {
    if (value == null) {
      return null;
    }
    Class<?> clazz = value.getClass();
    if (!clazz.isArray()) {
      return value;
    }
    Object result = null;
    if (clazz.getComponentType().isPrimitive()) {
      boolean[] input = (boolean[]) value;
      result = new Boolean[input.length];
      for (int i = 0; i < input.length; i++) {
        ((Boolean[]) result)[i] = Boolean.valueOf(input[i]);
      }
    } else {
      Boolean[] input = (Boolean[]) value;
      result = new boolean[input.length];
      for (int i = 0; i < input.length; i++) {
        ((boolean[]) result)[i] = input[i].booleanValue();
      }
    }
    return result;
  }
}
