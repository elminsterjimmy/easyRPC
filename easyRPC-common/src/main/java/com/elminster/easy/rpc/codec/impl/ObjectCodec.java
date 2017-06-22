package com.elminster.easy.rpc.codec.impl;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

public class ObjectCodec implements RpcCodec {

  private static Logger logger = LoggerFactory.getLogger(ObjectCodec.class);

  public Object decode(InputStream iStream, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      return encodingFactory.readObjectNullable(iStream);
    } catch (RpcException k) {
      throw k;
    } catch (Exception e) {
      String message = "Could not decode Object - " + e;
      logger.error(message, e);
      throw new RpcException(message);
    }
  }

  public void encode(OutputStream oStream, Object value, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      encodingFactory.writeObjectNullable(oStream, value);
    } catch (RpcException k) {
      throw k;
    } catch (Exception e) {
      String message = "Could not decode Object - " + e;
      logger.error(message, e);
      throw new RpcException(message);
    }
  }
}