package com.elminster.easy.rpc.codec.impl;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.util.RpcUtil;

public class StringCodec implements RpcCodec {

  private static Logger logger = LoggerFactory.getLogger(StringCodec.class);
  
  // TODO inject
  private RpcUtil rpcUtil;

  public Object decode(InputStream iStream, RpcEncodingFactory encodingFactory) throws RpcException {
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

  public void encode(OutputStream oStream, Object value, RpcEncodingFactory encodingFactory) throws RpcException {
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
