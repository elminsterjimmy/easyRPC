package com.elminster.easy.rpc.serializer.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryRepository;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.AsyncResponseProtocol;
import com.elminster.easy.rpc.protocol.impl.ProtocolFactoryImpl;
import com.elminster.easy.rpc.request.AsyncQueryResponse;
import com.elminster.easy.rpc.serializer.Serializer;

/**
 * The Async Response Serializer.
 * 
 * @author jinggu
 * @version 1.0
 */
public class AsyncQueryResponseSerializer extends BaseSerializer implements Serializer<AsyncQueryResponse> {
  
  private AsyncResponseProtocol aRspPro;
  
  public AsyncQueryResponseSerializer(RpcEncodingFactoryRepository repository, Codec codec) {
    super(repository, codec);
    try {
      aRspPro = (AsyncResponseProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(AsyncResponseProtocol.class);
    } catch (ObjectInstantiationExcption e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] serialize(AsyncQueryResponse aResponse) throws IOException, RpcException {
    RpcEncodingFactory encodingFactory = defaultEncodingFactory;
    return writeToBytes(aResponse, encodingFactory, aRspPro);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AsyncQueryResponse deserialize(ByteBuffer message) throws IOException, RpcException {
    RpcEncodingFactory encodingFactory = defaultEncodingFactory;
    return readFromBytes(message, encodingFactory, aRspPro);
  }

}
