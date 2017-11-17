package com.elminster.easy.rpc.serializer.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.util.BinaryUtil;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryRepository;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.RequestHeaderProtocol;
import com.elminster.easy.rpc.protocol.RequestProtocol;
import com.elminster.easy.rpc.protocol.impl.ProtocolFactoryImpl;
import com.elminster.easy.rpc.request.RpcRequest;
import com.elminster.easy.rpc.serializer.Serializer;

/**
 * The Request Serializer.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcRequestSerializer extends BaseSerializer implements Serializer<RpcRequest> {

  private RequestHeaderProtocol requestHeaderProtocol;
  private RequestProtocol requestProtocol;

  public RpcRequestSerializer(RpcEncodingFactoryRepository repository, Codec codec) {
    super(repository, codec);
    try {
      requestHeaderProtocol = (RequestHeaderProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(RequestHeaderProtocol.class);
      requestProtocol = (RequestProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(RequestProtocol.class);
    } catch (ObjectInstantiationExcption e) {
      // should not happened
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] serialize(RpcRequest request) throws IOException, RpcException {
    RpcEncodingFactory encodingFactory = this.defaultEncodingFactory;
    try {
      String encodingName = request.getEncoding();
      byte[] requestHeader = writeToBytes(encodingName, encodingFactory, requestHeaderProtocol);
      if (!isUsingDefaultEncodingFactory(encodingName)) {
        encodingFactory = getEncodingFactory(encodingName);
      }
      byte[] req = writeToBytes(request, encodingFactory, requestProtocol);

      byte[] serialized = BinaryUtil.concatBytes(requestHeader, req);
      return serialized;
    } catch (IOException | RpcException e) {
      throw e;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcRequest deserialize(ByteBuffer message) throws IOException, RpcException {
    RpcEncodingFactory encodingFactory = this.defaultEncodingFactory;
    try {
      String encodingName = readFromBytes(message, encodingFactory, requestHeaderProtocol);
      if (!isUsingDefaultEncodingFactory(encodingName)) {
        encodingFactory = getEncodingFactory(encodingName);
      }
      return readFromBytes(message, encodingFactory, requestProtocol);
    } catch (IOException | RpcException e) {
      throw e;
    }
  }
}
