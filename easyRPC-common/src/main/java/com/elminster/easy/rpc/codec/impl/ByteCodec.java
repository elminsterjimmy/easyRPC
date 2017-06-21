package com.elminster.easy.rpc.codec.impl;

import com.elminster.easy.rpc.codec.RpcCodec;

public class ByteCodec implements RpcCodec {
  private static Logger log = LoggerFactory.getLogger(ByteCodec.class);

  public Object decode(InputStream iStream, KisRpcEncodingFactory encodingFactory) throws KisRpcException {
    try {
      byte streamValue = CoreServiceRegistry.INSTANCE.getKisRpcUtil().readByte(iStream);
      return Byte.valueOf(streamValue);
    } catch (Exception e) {
      log.error("Byte decode:", e);
      throw new KisRpcException("Could not decode Byte - " + e.getMessage());
    }
  }

  public void encode(OutputStream oStream, Object value, KisRpcEncodingFactory encodingFactory) throws KisRpcException {
    try {
      oStream.write(((Byte) value).byteValue());
    } catch (Exception e) {
      log.error("Byte encode:", e);
      throw new KisRpcException("Could not encode Byte - " + e.getMessage());
    }
  }

  public Object decode(InputStream iStream, Object codecData, KisRpcEncodingFactory encodingFactory) throws KisRpcException {
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
      byte[] input = (byte[]) value;
      result = new Byte[input.length];
      for (int i = 0; i < input.length; i++) {
        ((Byte[]) result)[i] = Byte.valueOf(input[i]);
      }
    } else {
      Byte[] input = (Byte[]) value;
      result = new byte[input.length];
      for (int i = 0; i < input.length; i++) {
        ((byte[]) result)[i] = input[i].byteValue();
      }
    }
    return result;
  }
}
