package com.elminster.easy.rpc.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

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
  public void write(byte[] bytes, int off, int len) throws IOException {
    out.write(bytes, off, len);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int read(byte[] bytes, int off, int len) throws IOException {
    return in.read(bytes, off, len);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flush() throws IOException {
    out.flush();
  }
}
