package com.elminster.easy.rpc.protocol.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.CodecException;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.Protocol;

/**
 * Base protocol implementation.
 * 
 * @author jinggu
 * @version 1.0
 */
abstract public class ProtocolImpl<T> implements Protocol<T> {
  
  private static final Logger logger = LoggerFactory.getLogger(ProtocolImpl.class);
  
  protected Map<String, Map<Codec, RpcEncodingFactory>> cache = new ConcurrentHashMap<>(); // to cache the encoding factory
  
  /** the encoding factory for transport. **/
  protected final RpcEncodingFactory encodingFactory;

  public ProtocolImpl(RpcEncodingFactory encodingFactory) {
    this.encodingFactory = encodingFactory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void encode(T data) throws IOException, CodecException {
    byte[] bytes = prepareWriteData(data);
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("message size [%d] to write.", bytes.length));
    }
    encodingFactory.writeInt32(bytes.length);
    encodingFactory.writen(bytes, 0, bytes.length);
    encodingFactory.flush();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T decode() throws IOException, CodecException {
    int size = encodingFactory.readInt32();
    // what happened if the size is 0
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("message size [%d] to read.", size));
    }
    if (size <= 0) {
      logger.warn(String.format("unexpected size of message [%d].", size));
    }
    byte[] bytes = encodingFactory.readn(0, size);
    ByteBuffer buf = ByteBuffer.wrap(bytes); // to byte buffer
    RpcEncodingFactory bufferReader = cloneEncodingFactory(encodingFactory, CoreCodecFactory.INSTANCE.getCoreCodec(buf));
    return readData(bufferReader);
  }

  /**
   * Read the data with specified encoding factory.
   * 
   * @param encodingFactory
   *          the encoding factory
   * @throws IOException
   *           on IO error
   * @throws RpcException
   *           on Rpc error
   */
  abstract T readData(RpcEncodingFactory encodingFactory) throws IOException, CodecException;

  /**
   * Prepare the writing data.
   * FIXME
   * 
   * @return the data to write
   * @throws IOException
   *           on IO error
   * @throws RpcException
   *           on Rpc error
   */
  private byte[] prepareWriteData(T data) throws IOException, CodecException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Codec coreCodec = CoreCodecFactory.INSTANCE.getCoreCodec(null, out);
      RpcEncodingFactory sizeCalc = cloneEncodingFactory(encodingFactory, coreCodec);
      writeData(sizeCalc, data);
      byte[] bytes = out.toByteArray();
      if (0 == bytes.length) {
        logger.warn(String.format("======\nef=[%s], data=[%s].", sizeCalc, data));
      }
      return bytes;
    }
  }

  /**
   * Write the data with specified encoding factory.
   * 
   * @param encodingFactory
   *          the encoding factory
   * @throws IOException
   *           on IO error
   * @throws RpcException
   *           on Rpc error
   */
  abstract void writeData(RpcEncodingFactory encodingFactory, T data) throws IOException, CodecException;

  protected RpcEncodingFactory cloneEncodingFactory(RpcEncodingFactory encodingFactory, Codec coreCodec) {
    RpcEncodingFactory cloned = null;
    Map<Codec, RpcEncodingFactory> map = cache.get(encodingFactory.getName());
    if (null == map) {
      cloned = encodingFactory.cloneEncodingFactory();
      map = new ConcurrentHashMap<>();
      map.put(coreCodec, cloned);
      cache.put(encodingFactory.getName(), map);
    } else {
      cloned = map.get(coreCodec);
      if (null == cloned) {
        cloned = encodingFactory.cloneEncodingFactory();
        map.put(coreCodec, cloned);
      }
    }
    cloned.setCodec(coreCodec);
    return cloned;
  }
}
