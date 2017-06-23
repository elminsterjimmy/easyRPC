package com.elminster.easy.rpc.codec.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.registery.CoreServiceRegistry;
import com.elminster.easy.rpc.util.RpcUtil;

/**
 * The Timestamp Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class TimestampCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(TimestampCodec.class);
  
  /** the RPC util. */
  private static final RpcUtil rpcUtil = CoreServiceRegistry.INSTANCE.getRpcUtil();

  /**
   * {@inheritDoc}
   */
  public void encode(final OutputStream oStream, final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (value != null) {
        rpcUtil.writeLongBigEndian(oStream, ((Timestamp) value).getTime());
      }
    } catch (Exception e) {
      logger.error("Timestamp encode:", e);
      throw new RpcException("Could not encode Timestamp - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public Object decode(final InputStream iStream, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      return new Timestamp(rpcUtil.readLongBigEndian(iStream));
    } catch (Exception e) {
      logger.error("Timestamp decode:", e);
      throw new RpcException("Could not decode Timestamp - " + e.getMessage());
    }
  }
}