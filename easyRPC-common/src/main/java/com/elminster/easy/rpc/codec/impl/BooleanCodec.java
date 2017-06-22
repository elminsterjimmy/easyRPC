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
 * Boolean Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class BooleanCodec implements RpcCodec {
  
  private static Logger log = LoggerFactory.getLogger(BooleanCodec.class);

  // TODO inject
  private RpcUtil rpcUtil;
  
  /**
   * {@inheritDoc}
   */
  public Object decode(InputStream in, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      byte streamValue = rpcUtil.readByte(in);
      if (1 == streamValue) {
        return Boolean.TRUE;
      }
      return Boolean.FALSE;
    } catch (Exception e) {
      log.error("Boolean decode:", e);
      throw new RpcException("Could not decode Boolean - " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void encode(OutputStream out, Object value, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (null != value) {
        if (((Boolean) value).booleanValue()) {
          out.write(1);
        } else {
          out.write(0);
        }
      }
    } catch (Exception e) {
      log.error("Boolean encode:", e);
      throw new RpcException("Could not encode Boolean - " + e.getMessage());
    }
  }
}
