package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol;

public class ConfirmFrameProtocolImpl extends ProtocolImpl implements ConfirmFrameProtocol {
  
  public ConfirmFrameProtocolImpl(RpcEncodingFactory encodingFactory) {
    super(encodingFactory);
  }

  private byte frame;

  @Override
  public void encode() throws IOException, RpcException {
    encodingFactory.writeInt8(frame);
  }

  @Override
  public void decode() throws IOException, RpcException {
    this.frame = encodingFactory.readInt8();
  }

  @Override
  public boolean expact(byte frame) throws IOException {
    try {
      decode();
    } catch (RpcException e) {
      ;
    }
    return this.frame == frame;
  }

  @Override
  public void nextFrame(byte frame) throws IOException {
    this.frame = frame;
    try {
      encode();
    } catch (RpcException e) {
      ;
    }
  }

  @Override
  public byte getFrame() {
    return this.frame;
  }
}
