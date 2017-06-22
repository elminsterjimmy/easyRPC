package com.elminster.easy.rpc.codec.impl;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.util.RpcUtil;

public class IntegerCodec implements RpcCodec {

  private static Logger log = LoggerFactory.getLogger(IntegerCodec.class);
  
  // TODO inject
  private RpcUtil rpcUtil;

  public void encode(OutputStream oStream, Object value, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (null != value) {
        rpcUtil.writeIntBigEndian(oStream, ((Integer) value).intValue());
      }
    } catch (Exception e) {
      log.error("Integer encode:", e);
      throw new RpcException("Could not encode Integer - " + e.getMessage());
    }
  }

  public Object decode(InputStream iStream, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      int value = rpcUtil.readIntBigEndian(iStream);
      return Integer.valueOf(value);
    } catch (Exception e) {
      log.error("Integer decode:", e);
      throw new RpcException("Could not decode Integer - " + e.getMessage());
    }
  }
}