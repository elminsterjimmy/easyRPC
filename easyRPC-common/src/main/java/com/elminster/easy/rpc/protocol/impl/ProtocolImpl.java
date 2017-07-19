package com.elminster.easy.rpc.protocol.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.elminster.easy.rpc.codec.CoreCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.Protocol;

abstract public class ProtocolImpl implements Protocol {

  protected final RpcEncodingFactory encodingFactory;
  protected final RpcEncodingFactory sizeCalc;
  protected int size;

  public ProtocolImpl(RpcEncodingFactory encodingFactory) {
    this.encodingFactory = encodingFactory;
    this.sizeCalc = encodingFactory.cloneEncodingFactory();
  }

  @Override
  public void encode() throws IOException, RpcException {
    byte[] bytes = prepareWriteData();
    encodingFactory.writeInt32(bytes.length);
    encodingFactory.writen(bytes, 0, bytes.length);
    encodingFactory.flush();
  }

  @Override
  public void decode() throws IOException, RpcException {
    this.size = encodingFactory.readInt32();
    readData(encodingFactory);
  }

  abstract void readData(RpcEncodingFactory encodingFactory) throws IOException, RpcException;

  public byte[] prepareWriteData() throws IOException, RpcException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      CoreCodec coreCodec = CoreCodecFactory.INSTANCE.getCoreCodec(null, out);
      sizeCalc.setCoreCodec(coreCodec);
      writeData(sizeCalc);
      return out.toByteArray();
    }
  }

  abstract void writeData(RpcEncodingFactory encodingFactory) throws IOException, RpcException;

  @Override
  public int getDataSize() throws IOException {
    return this.size;
  }
}
