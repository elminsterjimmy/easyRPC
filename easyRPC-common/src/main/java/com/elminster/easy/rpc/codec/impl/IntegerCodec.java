package com.elminster.easy.rpc.codec.impl;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.registery.CoreServiceRegistry;
import com.elminster.easy.rpc.util.RpcUtil;

public class IntegerCodec implements RpcCodec {

  private static Logger logger = LoggerFactory.getLogger(IntegerCodec.class);
  
  /** the RPC util. */
  private static final RpcUtil rpcUtil = CoreServiceRegistry.INSTANCE.getRpcUtil();

  public void encode(final OutputStream oStream, final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (null != value) {
        rpcUtil.writeIntBigEndian(oStream, ((Integer) value).intValue());
      }
    } catch (Exception e) {
      logger.error("Integer encode:", e);
      throw new RpcException("Could not encode Integer - " + e.getMessage());
    }
  }

  public Object decode(final InputStream iStream, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      int value = rpcUtil.readIntBigEndian(iStream);
      return Integer.valueOf(value);
    } catch (Exception e) {
      logger.error("Integer decode:", e);
      throw new RpcException("Could not decode Integer - " + e.getMessage());
    }
  }
}