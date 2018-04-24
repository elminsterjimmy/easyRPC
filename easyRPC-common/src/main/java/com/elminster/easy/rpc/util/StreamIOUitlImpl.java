package com.elminster.easy.rpc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Stream IO Utilities.
 * 
 * @author jinggu
 * @version 1.0
 */
public class StreamIOUitlImpl implements IoUtil {

  private final InputStream in;
  private final OutputStream out;
  
  public StreamIOUitlImpl(final InputStream in, final OutputStream out) {
    this.in = in;
    this.out = out;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int write(byte[] bytes, int off, int len) throws IOException {
    out.write(bytes, off, len);
    return len;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int read(byte[] bytes, int off, int len) throws IOException {
    return in.read(bytes, off, len);
  }
  
  public int write(ByteBuffer buffer) throws IOException {
    return write(buffer.array(), 0, buffer.capacity());
  }
  
  public int read(ByteBuffer buffer) throws IOException {
    byte[] bytes = new byte[buffer.capacity()];
    int size = read(bytes, 0, bytes.length);
    buffer.put(bytes);
    return size;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flush() throws IOException {
    out.flush();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
  }
}
