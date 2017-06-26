package com.elminster.easy.rpc.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class RpcUtilFactory {
  
  public static final RpcUtilFactory INSTANCE = new RpcUtilFactory();
  
  private RpcUtilFactory() {}

  public RpcUtil getRpcUtil(InputStream in, OutputStream out) {
    IoUtil ioUtil = new StreamIOUitlImpl(in, out);
    return new RpcUtilImpl(ioUtil);
  }

  public RpcUtil getRpcUtil(ByteBuffer byteBuffer) {
    IoUtil ioUtil = new ByteBufferIoImpl(byteBuffer);
    return new RpcUtilImpl(ioUtil);
  }
  
  public RpcUtil getRpcUtil(SocketChannel socketChannel) {
    IoUtil ioUtil = new NioChannelUtil(socketChannel);
    return new RpcUtilImpl(ioUtil);
  }
}
