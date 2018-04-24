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
  private final ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
  private ByteBuffer dataBuffer;
  private boolean sizeRead = false;;

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
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("message size [%d] written.", bytes.length));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T decode() throws IOException, CodecException {
    logger.debug("Protocol#decode()");
    if (!sizeRead) {
      encodingFactory.readn(sizeBuffer);
      if (sizeBuffer.hasRemaining()) {
        // not ready
        return null;
      } else {
        sizeBuffer.flip();
        int size = sizeBuffer.getInt();
        if (logger.isDebugEnabled()) {
          logger.debug(String.format("message size [%d] to read.", size));
        }
        sizeBuffer.flip();
        dataBuffer = ByteBuffer.allocateDirect(size);
        sizeRead = true;
      }
    }
    if (sizeRead) {
      encodingFactory.readn(dataBuffer);
      if (dataBuffer.hasRemaining()) {
        // not ready
        return null;
      } else {
        dataBuffer.rewind();
        RpcEncodingFactory bufferReader = cloneEncodingFactory(encodingFactory, CoreCodecFactory.INSTANCE.getCoreCodec(dataBuffer));
        T data = readData(bufferReader);
        if (logger.isDebugEnabled()) {
          logger.debug(String.format("message size [%d] read.", dataBuffer.limit()));
        }
        // release the mem
        ((sun.nio.ch.DirectBuffer)dataBuffer).cleaner().clean();
        sizeRead = false;
        return data;
      }
    }
    return null;
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
