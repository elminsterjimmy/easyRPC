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

/**
 * Double Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public final class DoubleCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(DoubleCodec.class);
  
  /** the RPC util. */
  private static final RpcUtil rpcUtil = CoreServiceRegistry.INSTANCE.getRpcUtil();

  /**
   * {@inheritDoc}
   */
  public void encode(final OutputStream oStream, final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      long bits = Double.doubleToLongBits(((Double) value).doubleValue());

      rpcUtil.writeLongBigEndian(oStream, bits);
    } catch (Exception e) {
      logger.error("Double encode:", e);
      throw new RpcException("Could not encode Double - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public Object decode(InputStream iStream, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      long bits = rpcUtil.readLongBigEndian(iStream);
      return new Double(Double.longBitsToDouble(bits));
    } catch (Exception e) {
      logger.error("Double decode:", e);
      throw new RpcException("Could not decode Double - " + e.getMessage());
    }
  }
}
