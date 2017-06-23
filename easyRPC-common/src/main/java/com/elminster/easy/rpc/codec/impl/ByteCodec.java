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
 * Byte Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ByteCodec implements RpcCodec {
  
  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(ByteCodec.class);
  
  /** the RPC util. */
  private static final RpcUtil rpcUtil = CoreServiceRegistry.INSTANCE.getRpcUtil();

  /**
   * {@inheritDoc}
   */
  public Object decode(final InputStream iStream, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      byte streamValue = rpcUtil.readByte(iStream);
      return Byte.valueOf(streamValue);
    } catch (Exception e) {
      logger.error("Byte decode:", e);
      throw new RpcException("Could not decode Byte - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void encode(final OutputStream oStream, final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      oStream.write(((Byte) value).byteValue());
    } catch (Exception e) {
      logger.error("Byte encode:", e);
      throw new RpcException("Could not encode Byte - " + e.getMessage());
    }
  }
}
