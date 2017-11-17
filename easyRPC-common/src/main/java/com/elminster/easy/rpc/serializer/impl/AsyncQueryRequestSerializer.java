package com.elminster.easy.rpc.serializer.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryRepository;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.AsyncRequestProtocol;
import com.elminster.easy.rpc.protocol.impl.ProtocolFactoryImpl;
import com.elminster.easy.rpc.request.AsyncQueryRequest;
import com.elminster.easy.rpc.serializer.Serializer;

/**
 * The Async Request Serializer.
 * 
 * @author jinggu
 * @version 1.0
 */
public class AsyncQueryRequestSerializer extends BaseSerializer implements Serializer<AsyncQueryRequest> {

  private AsyncRequestProtocol aReqPro;

  public AsyncQueryRequestSerializer(RpcEncodingFactoryRepository repository, Codec codec) {
    super(repository, codec);
    try {
      aReqPro = (AsyncRequestProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(AsyncRequestProtocol.class);
    } catch (ObjectInstantiationExcption e) {
      // should not happened
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] serialize(AsyncQueryRequest request) throws IOException, RpcException {
    RpcEncodingFactory encodingFactory = defaultEncodingFactory;
    return writeToBytes(request, encodingFactory, aReqPro);
    
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AsyncQueryRequest deserialize(ByteBuffer message) throws IOException, RpcException {
    RpcEncodingFactory encodingFactory = defaultEncodingFactory;
    return readFromBytes(message, encodingFactory, aReqPro);
  }
}
