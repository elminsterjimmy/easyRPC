package com.elminster.easy.rpc.util;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * NIO channel utilities.
 * 
 * @author jinggu
 * @version 1.0
 */
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
  
  public NioChannelUtil(SocketChannel socketChannel) {
    this.socketChannel = socketChannel;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(byte[] bytes, int off, int len) throws IOException {
    int curLen = len;
    while (curLen > 0) {
      int size = Math.min(writeByteBuffer.get().capacity(), curLen);
      writeByteBuffer.get().put(bytes, off, size);
      writeByteBuffer.get().flip();
      socketChannel.write(writeByteBuffer.get());
      writeByteBuffer.get().clear();
      curLen -= writeByteBuffer.get().capacity();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int read(byte[] bytes, int off, int len) throws IOException {
    int readBytes = socketChannel.read(readByteBuffer.get());
    readByteBuffer.get().flip();
    readBytes = Math.min(readByteBuffer.get().remaining(), len);
    if (readBytes > 0) {
      try {
        // debug read byte buffer.
        readByteBuffer.get().rewind();
        byte[] debug = readByteBuffer.get().array();
        String bufferValue = "";
        for (int i = 0; i < debug.length; i++) {
          bufferValue += "" + debug[i] + "|";
        }
        System.err.println(bufferValue);
        readByteBuffer.get().rewind();
        readByteBuffer.get().get(bytes, off, readBytes);
        readByteBuffer.get().compact();
      } catch (BufferUnderflowException e) {
        throw new IOException("Buffer Underflow!", e);
      }
    }
    return readBytes;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flush() throws IOException {
  }
}
