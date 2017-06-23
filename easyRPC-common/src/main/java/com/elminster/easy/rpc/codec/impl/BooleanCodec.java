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
 * Boolean Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class BooleanCodec implements RpcCodec {
  
  private static Logger logger = LoggerFactory.getLogger(BooleanCodec.class);

  /** the RPC util. */
  private static final RpcUtil rpcUtil = CoreServiceRegistry.INSTANCE.getRpcUtil();
  
  /**
   * {@inheritDoc}
   */
  public Object decode(final InputStream iStream, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      byte streamValue = rpcUtil.readByte(iStream);
      if (1 == streamValue) {
        return Boolean.TRUE;
      }
      return Boolean.FALSE;
    } catch (Exception e) {
      logger.error("Boolean decode:", e);
      throw new RpcException("Could not decode Boolean - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void encode(final OutputStream oStream, final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (null != value) {
        if (((Boolean) value).booleanValue()) {
          oStream.write(1);
        } else {
          oStream.write(0);
        }
      }
    } catch (Exception e) {
      logger.error("Boolean encode:", e);
      throw new RpcException("Could not encode Boolean - " + e.getMessage());
    }
  }
}
