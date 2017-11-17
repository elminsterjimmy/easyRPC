package com.elminster.easy.rpc.serializer.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryNotFoundException;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryRepository;
import com.elminster.easy.rpc.encoding.impl.DefaultRpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.Protocol;

/**
 * The Base Serializer.
 * 
 * @author jinggu
 * @version 1.0
 */
public class BaseSerializer {

  protected RpcEncodingFactory defaultEncodingFactory;
  protected RpcEncodingFactoryRepository encodingFactoryRepository;
  protected Codec codec;

  public BaseSerializer(RpcEncodingFactoryRepository repository, Codec codec) {
    this.encodingFactoryRepository = repository;
    this.codec = codec;
    try {
      this.defaultEncodingFactory = getEncodingFactory(DefaultRpcEncodingFactory.ENCODING_FACTORY_NAME);
    } catch (RpcEncodingFactoryNotFoundException e) {
      // should not happened
      e = null;
    }
  }

  /**
   * Get the encoding factory by encoding factory name.
   * 
   * @param encodingFactoryName
   *          the encoding factory name
   * @return encoding factory with specified encoding factory name
   * @throws RpcEncodingFactoryNotFoundException
   *           if the encoding factory is not found
   */
  protected RpcEncodingFactory getEncodingFactory(String encodingFactoryName) throws RpcEncodingFactoryNotFoundException {
    return encodingFactoryRepository.getRpcEncodingFactory(encodingFactoryName, codec).cloneEncodingFactory();
  }

  /**
   * Check if the request uses default encoding factory.
   * 
   * @param encodingName
   *          the encoding factory name
   * @return if the request uses default encoding factory
   */
  protected boolean isUsingDefaultEncodingFactory(String encodingName) {
    return DefaultRpcEncodingFactory.ENCODING_FACTORY_NAME.equals(encodingName);
  }
  
  
  
  protected <T> byte[] writeToBytes(T message, RpcEncodingFactory encodingFactory, Protocol<T> protocol) throws IOException, RpcException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Codec coreCodec = CoreCodecFactory.INSTANCE.getCoreCodec(null, out);
      encodingFactory.setCodec(coreCodec);
      protocol.encode(message, encodingFactory);
      return out.toByteArray();
    }
  }
    
  protected <T> T readFromBytes(ByteBuffer byteBuffer, RpcEncodingFactory encodingFactory, Protocol<T> protocol) throws IOException, RpcException {
    Codec coreCodec = CoreCodecFactory.INSTANCE.getCoreCodec(byteBuffer);
    encodingFactory.setCodec(coreCodec);
    return protocol.decode(encodingFactory);
  }
}
