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
 * The String Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class StringCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(StringCodec.class);
  
  /** the RPC util. */
  private static final RpcUtil rpcUtil = CoreServiceRegistry.INSTANCE.getRpcUtil();

  /**
   * {@inheritDoc}
   */
  public Object decode(final InputStream iStream, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      int length = rpcUtil.readIntBigEndian(iStream);
      byte[] encValueBytes = new byte[length];
      rpcUtil.readn(iStream, encValueBytes, 0, length);
      return new String(encValueBytes, "UTF-8");
    } catch (Exception e) {
      logger.error("String decode:", e);
      throw new RpcException("Could not decode String - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void encode(final OutputStream oStream, final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (null != value) {
        String encValue = (String) value;
        
        byte[] encValueBytes = encValue.getBytes("UTF-8");
        rpcUtil.writeIntBigEndian(oStream, encValueBytes.length);
        oStream.write(encValueBytes);
      }
    } catch (Exception e) {
      logger.error("String encode:", e);
      throw new RpcException("Could not encode String - " + e.getMessage());
    }
  }
}