package com.elminster.easy.rpc.codec.impl;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.util.RpcUtil;

public class LongCodec implements RpcCodec {

  private static Logger log = LoggerFactory.getLogger(LongCodec.class);
  
  // TODO inject
  private RpcUtil rpcUtil;

  public void encode(OutputStream oStream, Object value, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (null != value) {
      rpcUtil.writeLongBigEndian(oStream, ((Long) value).longValue());
      }
    } catch (Exception e) {
      log.error("Long encode:", e);
      throw new RpcException("Could not encode Long - " + e.getMessage());
    }
  }

  public Object decode(InputStream iStream, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      long value = rpcUtil.readLongBigEndian(iStream);
      return Long.valueOf(value);
    } catch (Exception e) {
      log.error("Long decode:", e);
      throw new RpcException("Could not decode Long - " + e.getMessage());
    }
  }
}
