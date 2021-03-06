package com.elminster.easy.rpc.codec;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The interface for fundamental encoding and decoding.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface Codec extends Closeable {

  /**
   * Get the core codec name.
   * @return the core codec name
   */
  public String getName();

  /**
   * Write a byte value.
   * 
   * @param byteValue
   *          the byte value
   * @throws IOException
   *           on error
   */
  public void writeByte(byte byteValue) throws IOException;

  /**
   * Read a byte value.
   * 
   * @return the byte value
   * @throws IOException
   *           on error
   */
  public byte readByte() throws IOException;

  /**
   * Write a int value with big endian.
   * 
   * @param intValue
   *          the int value
   * @throws IOException
   *           on error
   */
  public void writeIntBigEndian(int intValue) throws IOException;

  /**
   * Read a int value with big endian.
   * 
   * @return the int value
   * @throws IOException
   *           on error
   */
  public int readIntBigEndian() throws IOException;

  /**
   * Write a long value with big endian.
   * 
   * @param longValue
   *          the long value
   * @throws IOException
   *           on error
   */
  public void writeLongBigEndian(long longValue) throws IOException;

  /**
   * Read a long value with big endian.
   * 
   * @return the long value
   * @throws IOException
   *           on error
   */
  public long readLongBigEndian() throws IOException;

  /**
   * Write a byte array.
   * 
   * @param b
   *          the byte array
   * @param offset
   *          the offset
   * @param len
   *          the length to write
   * @throws IOException
   *           on error
   */
  public void writen(byte[] bytes, int off, int len) throws IOException;
  
  /**
   * Read into a byte array.
   * 
   * @param b
   *          the byte array
   * @param offset
   *          the offset
   * @param len
   *          the length to read
   * @throws IOException
   *           on error
   */
  public void readn(byte[] b, int offset, int len) throws IOException;
  
  /**
   * Read into a byte buffer.
   * 
   * @param buffer
   *          the byte buffer
   * @throws IOException
   *           on error
   */
  public void writen(ByteBuffer buffer) throws IOException;
  
  /**
   * Write a byte buffer.
   * 
   * @param buffer
   *          the byte buffer
   * @throws IOException
   *           on error
   */
  public void readn(ByteBuffer buffer) throws IOException;

  /**
   * Write an ASCII String.
   * 
   * @param asciiString
   *          the ASCII String
   * @throws IOException
   *           on error
   */
  public void writeStringAsciiNullable(String asciiString) throws IOException;

  /**
   * Write an UTF8 String.
   * 
   * @param utf8String
   *          the UTF8 String
   * @throws IOException
   *           on error
   */
  public void writeStringUTF8Nullable(String utf8String) throws IOException;

  /**
   * Read an ASCII String.
   * 
   * @return the ASCII String
   * @throws IOException
   *           on error
   */
  public String readStringAsciiNullable() throws IOException;

  /**
   * Read an UTF8 String.
   * 
   * @return the UTF8 String
   * @throws IOException
   *           on error
   */
  public String readStringUTF8Nullable() throws IOException;

  /**
   * Flush to underlayer stream.
   * 
   * @throws IOException
   *           on error
   */
  public void flush() throws IOException;
  
  /**
   * Clean up resources.
   */
  public void close();
}
