package com.elminster.easy.rpc.codec.impl;

import com.elminster.easy.rpc.codec.RpcCodec;

public final class DoubleCodec implements RpcCodec {
  private static Logger log = LoggerFactory.getLogger(DoubleCodec.class);

  public void encode(OutputStream oStream, Object value, KisRpcEncodingFactory encodingFactory) throws KisRpcException {
    try {
      long bits = Double.doubleToLongBits(((Double) value).doubleValue());

      CoreServiceRegistry.INSTANCE.getKisRpcUtil().writeLongBigEndian(oStream, bits);
    } catch (Exception e) {
      log.error("Double encode:", e);
      throw new KisRpcException("Could not encode Double - " + e.getMessage());
    }
  }

  public Object decode(InputStream iStream, KisRpcEncodingFactory encodingFactory) throws KisRpcException {
    try {
      long bits = CoreServiceRegistry.INSTANCE.getKisRpcUtil().readLongBigEndian(iStream);
      return new Double(Double.longBitsToDouble(bits));
    } catch (Exception e) {
      log.error("Double decode:", e);
      throw new KisRpcException("Could not decode Double - " + e.getMessage());
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
      double[] input = (double[]) value;
      result = new Double[input.length];
      for (int i = 0; i < input.length; i++) {
        ((Double[]) result)[i] = Double.valueOf(input[i]);
      }
    } else {
      Double[] input = (Double[]) value;
      result = new double[input.length];
      for (int i = 0; i < input.length; i++) {
        ((double[]) result)[i] = input[i].doubleValue();
      }
    }
    return result;
  }
}
