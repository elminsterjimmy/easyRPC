package com.elminster.easy.rpc.serializer.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.util.BinaryUtil;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryRepository;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.ResponseHeaderProtocol;
import com.elminster.easy.rpc.protocol.ResponseProtocol;
import com.elminster.easy.rpc.protocol.impl.ProtocolFactoryImpl;
import com.elminster.easy.rpc.request.Response;
import com.elminster.easy.rpc.serializer.Serializer;

/**
 * The Response Serializer.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ResponseSerializer extends BaseSerializer implements Serializer<Response> {

  private ResponseHeaderProtocol responseHeaderProtocol;
  private ResponseProtocol responseProtocol;

  public ResponseSerializer(RpcEncodingFactoryRepository repository, Codec codec) {
    super(repository, codec);
    try {
      responseHeaderProtocol = (ResponseHeaderProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ResponseHeaderProtocol.class);
      this.responseProtocol = (ResponseProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ResponseProtocol.class);
    } catch (ObjectInstantiationExcption e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] serialize(Response response) throws IOException, RpcException {
    RpcEncodingFactory encodingFactory = this.defaultEncodingFactory;
    try {
      String encodingName = response.getEncoding();
      byte[] requestHeader = writeToBytes(encodingName, encodingFactory, responseHeaderProtocol);
      if (!isUsingDefaultEncodingFactory(encodingName)) {
        encodingFactory = getEncodingFactory(encodingName);
      }
      byte[] req = writeToBytes(response, encodingFactory, responseProtocol);
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
  public Response deserialize(ByteBuffer message) throws IOException, RpcException {
    RpcEncodingFactory encodingFactory = this.defaultEncodingFactory;
    try {
      String encodingName = readFromBytes(message, encodingFactory, responseHeaderProtocol);
      if (!isUsingDefaultEncodingFactory(encodingName)) {
        encodingFactory = getEncodingFactory(encodingName);
      }
      return readFromBytes(message, encodingFactory, responseProtocol);
    } catch (IOException | RpcException e) {
      throw e;
    }
  }
}
