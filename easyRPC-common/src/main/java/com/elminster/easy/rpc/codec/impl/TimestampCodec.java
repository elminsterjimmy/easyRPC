package com.elminster.easy.rpc.codec.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.util.RpcUtil;

public class TimestampCodec implements RpcCodec {

  private static Logger logger = LoggerFactory.getLogger(TimestampCodec.class);
  
  // TODO inject
  private RpcUtil rpcUtil;

  public void encode(OutputStream oStream, Object value, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (value != null) {
        rpcUtil.writeLongBigEndian(oStream, ((Timestamp) value).getTime());
      }
    } catch (Exception e) {
      logger.error("Timestamp encode:", e);
      throw new RpcException("Could not encode Timestamp - " + e.getMessage());
    }
  }

  public Object decode(InputStream iStream, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      return new Timestamp(rpcUtil.readLongBigEndian(iStream));
    } catch (Exception e) {
      logger.error("Timestamp decode:", e);
      throw new RpcException("Could not decode Timestamp - " + e.getMessage());
    }
  }
}
