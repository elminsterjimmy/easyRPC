package com.elminster.easy.rpc.codec.impl;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
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
  
  // TODO inject
  private RpcUtil rpcUtil;

  /**
   * {@inheritDoc}
   */
  public Object decode(InputStream in, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      byte streamValue = rpcUtil.readByte(in);
      return Byte.valueOf(streamValue);
    } catch (Exception e) {
      logger.error("Byte decode:", e);
      throw new RpcException("Could not decode Byte - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void encode(OutputStream out, Object value, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      out.write(((Byte) value).byteValue());
    } catch (Exception e) {
      logger.error("Byte encode:", e);
      throw new RpcException("Could not encode Byte - " + e.getMessage());
    }
  }
}
