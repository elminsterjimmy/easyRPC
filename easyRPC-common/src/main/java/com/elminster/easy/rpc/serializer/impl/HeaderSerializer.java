package com.elminster.easy.rpc.serializer.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.util.BinaryUtil;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryRepository;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.RequestTypeProtocol;
import com.elminster.easy.rpc.protocol.ShakehandProtocol;
import com.elminster.easy.rpc.protocol.VersionProtocol;
import com.elminster.easy.rpc.protocol.impl.ProtocolFactoryImpl;
import com.elminster.easy.rpc.request.Header;
import com.elminster.easy.rpc.request.impl.HeaderImpl;
import com.elminster.easy.rpc.serializer.Serializer;

/**
 * The header serializer.
 * 
 * @author jinggu
 * @version 1.0
 */
public class HeaderSerializer extends BaseSerializer implements Serializer<Header> {

  private ShakehandProtocol shakehandProtocol;
  private VersionProtocol versionProtocol;
  private RequestTypeProtocol reqTypeProtocol;

  public HeaderSerializer(RpcEncodingFactoryRepository repository, Codec codec) {
    super(repository, codec);
    try {
      shakehandProtocol = (ShakehandProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ShakehandProtocol.class);
      versionProtocol = (VersionProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(VersionProtocol.class);
      reqTypeProtocol = (RequestTypeProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(RequestTypeProtocol.class);
    } catch (ObjectInstantiationExcption e) {
      // should not happened
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] serialize(Header header) throws IOException, RpcException {
    try {
      byte[] shakehand = writeToBytes(null, defaultEncodingFactory, shakehandProtocol);
      byte[] version = writeToBytes(header.getVersion(), defaultEncodingFactory, versionProtocol);
      byte[] type = writeToBytes(header.getRequestType(), defaultEncodingFactory, reqTypeProtocol);
      byte[] serialized = BinaryUtil.concatBytes(shakehand, version, type);
      return serialized;
    } catch (IOException | RpcException e) {
      throw e;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Header deserialize(ByteBuffer message) throws IOException, RpcException {
    try {
      readFromBytes(message, defaultEncodingFactory, shakehandProtocol);
      HeaderImpl header = new HeaderImpl();
      String version = readFromBytes(message, defaultEncodingFactory, versionProtocol);
      header.setVersion(version);
      byte type = readFromBytes(message, defaultEncodingFactory, reqTypeProtocol);
      header.setRequestType(type);
      return header;
    } catch (IOException | RpcException e) {
      throw e;
    }
  }
}
