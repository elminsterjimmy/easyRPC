package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.Protocol;

/**
 * The base Protocol.
 * 
 * @author jinggu
 * @version 1.0
 * @param <T> generic type
 */
abstract public class ProtocolImpl<T> implements Protocol<T> {

  public ProtocolImpl() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void encode(T message, RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    writeData(message, encodingFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T decode(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    return readData(encodingFactory);
  }

  abstract T readData(RpcEncodingFactory encodingFactory) throws IOException, RpcException;

//  private byte[] prepareWriteData(T message, RpcEncodingFactory encodingFactory) throws IOException, RpcException {
//    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//      Codec coreCodec = CoreCodecFactory.INSTANCE.getCoreCodec(null, out);
//      encodingFactory.setCodec(coreCodec);
//      writeData(message, encodingFactory);
//      return out.toByteArray();
//    }
//  }

  abstract void writeData(T message, RpcEncodingFactory encodingFactory) throws IOException, RpcException;
}
