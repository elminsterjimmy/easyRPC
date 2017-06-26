package com.elminster.easy.rpc.util;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ByteBufferIoImpl implements IoUtil {

  private final ByteBuffer byteBuffer;
  
  public ByteBufferIoImpl(final ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int read(byte[] buf, int off, int len) throws IOException {
    final int n = Math.min(byteBuffer.remaining(), len);
    if (n > 0) {
      try {
        byteBuffer.rewind();
        byteBuffer.get(buf, off, n);
      } catch (BufferUnderflowException e) {
        throw new IOException("Buffer Underflow!", e);
      }
    }
    return n;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(byte[] buf, int off, int len) throws IOException {
    try {
      byteBuffer.put(buf, off, len);
    } catch (BufferOverflowException e) {
      throw new IOException("Buffer Overflow!", e);
    }
  }
}
