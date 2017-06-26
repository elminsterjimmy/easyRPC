package com.elminster.easy.rpc.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioChannelUtil implements IoUtil {
  
  private SocketChannel socketChannel;
  private ByteBuffer writeByteBuffer = ByteBuffer.allocate(1024);
  private ByteBuffer readByteBuffer = ByteBuffer.allocate(1024);
  private ByteBufferIoImpl readIo = new ByteBufferIoImpl(readByteBuffer);
  private ByteBufferIoImpl writeIo = new ByteBufferIoImpl(writeByteBuffer);
  
  public NioChannelUtil(SocketChannel socketChannel) {
    this.socketChannel = socketChannel;
  }

  @Override
  public void write(byte[] bytes, int off, int len) throws IOException {
    int curLen = len;
    while (curLen > 0) {
      int size = Math.min(writeByteBuffer.capacity(), curLen);
      writeIo.write(bytes, 0, size);
      writeByteBuffer.flip();
      socketChannel.write(writeByteBuffer);
      writeByteBuffer.clear();
      curLen -= writeByteBuffer.capacity();
    }
  }

  @Override
  public int read(byte[] bytes, int off, int len) throws IOException {
    int readBytes = socketChannel.read(readByteBuffer);
    readByteBuffer.rewind();
    readBytes = readIo.read(bytes, off, len);
    readByteBuffer.compact();
    return readBytes;
  }
}
