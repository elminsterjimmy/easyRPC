package com.elminster.easy.rpc.util;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.elminster.easy.rpc.buffer.BufferPool;

/**
 * NIO channel utilities.
 * 
 * @author jinggu
 * @version 1.0
 */
public class NioChannelUtil implements IoUtil {
  
//  private static final Logger logger = LoggerFactory.getLogger(NioChannelUtil.class);

  private final SocketChannel socketChannel;

  /**
   * The shared buffer pool.
   */
  private static ThreadLocal<BufferPool> bufferPool = new ThreadLocal<BufferPool>() {

    @Override
    protected BufferPool initialValue() {
      return new BufferPool();
    }
  };

  private final ByteBuffer writeByteBuffer;
  private final ByteBuffer readByteBuffer;

  public NioChannelUtil(SocketChannel socketChannel) {
    this.socketChannel = socketChannel;
    writeByteBuffer = bufferPool.get().borrow(8192);
    readByteBuffer = bufferPool.get().borrow(8192);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int write(byte[] bytes, int off, int len) throws IOException {
    int size = Math.min(writeByteBuffer.capacity(), len);
    writeByteBuffer.put(bytes, off, size);
    writeByteBuffer.flip();
    int written = socketChannel.write(writeByteBuffer);
    writeByteBuffer.clear();
    return written;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int read(byte[] bytes, int off, int len) throws IOException {
    int readBytes = socketChannel.read(readByteBuffer);
    readByteBuffer.flip();
    if (readBytes >= 0) {
      readBytes = Math.min(readByteBuffer.remaining(), len);
      try {
        readByteBuffer.get(bytes, off, readBytes);
        readByteBuffer.compact();
      } catch (BufferUnderflowException e) {
        throw new IOException("Buffer Underflow!", e);
      }
    }
    return readBytes;
  }
  
  public int read(ByteBuffer buffer) throws IOException {
    return socketChannel.read(buffer);
  }
  
  public int write(ByteBuffer buffer) throws IOException {
    return socketChannel.write(buffer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flush() throws IOException {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    bufferPool.get().release(readByteBuffer);
    bufferPool.get().release(writeByteBuffer);
  }
}
