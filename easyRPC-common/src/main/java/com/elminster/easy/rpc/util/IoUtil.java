package com.elminster.easy.rpc.util;

import java.io.IOException;

/**
 * The IOUtil.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface IoUtil {

  /**
   * write bytes.
   * 
   * @param bytes
   *          the bytes to write
   * @param off
   *          the offset
   * @param len
   *          the length
   * @throws IOException
   *           on error
   */
  public void write(byte[] bytes, int off, int len) throws IOException;

  /**
   * read bytes.
   * 
   * @param bytes
   *          the bytes to read
   * @param off
   *          the offset
   * @param len
   *          the length
   * @return read size
   * @throws IOException
   *           on error
   */
  public int read(byte[] bytes, int off, int len) throws IOException;

  /**
   * Flush to underlayer stream.
   * 
   * @throws IOException
   *           on error
   */
  public void flush() throws IOException;
}
