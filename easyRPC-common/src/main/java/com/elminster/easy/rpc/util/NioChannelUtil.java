package com.elminster.easy.rpc.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioChannelUtil implements IoUtil {
  
  private SocketChannel socketChannel;
  
  /** shared byte buffer. */
  private static ThreadLocal<ByteBuffer> writeByteBuffer = new ThreadLocal<ByteBuffer>() {

    protected ByteBuffer initialValue() {
      return ByteBuffer.allocate(1024);
    }
  };
  private static ThreadLocal<ByteBuffer> readByteBuffer = new ThreadLocal<ByteBuffer>() {
    
    protected ByteBuffer initialValue() {
      return ByteBuffer.allocate(1024);
    }
  };
  
  private ByteBufferIoImpl readIo = new ByteBufferIoImpl(readByteBuffer.get());
  private ByteBufferIoImpl writeIo = new ByteBufferIoImpl(writeByteBuffer.get());
  
  public NioChannelUtil(SocketChannel socketChannel) {
    this.socketChannel = socketChannel;
  }

  @Override
  public void write(byte[] bytes, int off, int len) throws IOException {
    int curLen = len;
    while (curLen > 0) {
      int size = Math.min(writeByteBuffer.get().capacity(), curLen);
      writeIo.write(bytes, 0, size);
      writeByteBuffer.get().flip();
      socketChannel.write(writeByteBuffer.get());
      writeByteBuffer.get().clear();
      curLen -= writeByteBuffer.get().capacity();
    }
  }

  @Override
  public int read(byte[] bytes, int off, int len) throws IOException {
    int readBytes = socketChannel.read(readByteBuffer.get());
    readByteBuffer.get().rewind();
    readBytes = readIo.read(bytes, off, len);
    readByteBuffer.get().compact();
    return readBytes;
  }
}
