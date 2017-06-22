package com.elminster.easy.rpc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The interface for fundamental encoding and decoding.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcUtil {

  /**
   * Write a byte value.
   * 
   * @param out
   *          the output stream
   * @param byteValue
   *          the byte value
   * @throws IOException
   *           on error
   */
  public void writeByte(OutputStream out, byte byteValue) throws IOException;

  /**
   * Read a byte value.
   * 
   * @param in
   *          the input stream
   * @return the byte value
   * @throws IOException
   *           on error
   */
  public byte readByte(InputStream in) throws IOException;

  /**
   * Write a int value with big endian.
   * 
   * @param out
   *          the output stream
   * @param intValue
   *          the int value
   * @throws IOException
   *           on error
   */
  public void writeIntBigEndian(OutputStream out, int intValue) throws IOException;

  /**
   * Read a int value with big endian.
   * 
   * @param in
   *          the input stream
   * @return the int value
   * @throws IOException
   *           on error
   */
  public int readIntBigEndian(InputStream in) throws IOException;

  /**
   * Write a long value with big endian.
   * 
   * @param out
   *          the output stream
   * @param longValue
   *          the long value
   * @throws IOException
   *           on error
   */
  public void writeLongBigEndian(OutputStream out, long longValue) throws IOException;

  /**
   * Read a long value with big endian.
   * 
   * @param in
   *          the input stream
   * @return the long value
   * @throws IOException
   *           on error
   */
  public long readLongBigEndian(InputStream in) throws IOException;

  /**
   * Read into a byte array.
   * 
   * @param in
   *          the input stream
   * @param b
   *          the byte array
   * @param offset
   *          the offset
   * @param len
   *          the length to read
   * @throws IOException
   *           on error
   */
  public void readn(InputStream in, byte[] b, int off, int len) throws IOException;

  /**
   * Write an ASCII String.
   * 
   * @param out
   *          the output stream
   * @param asciiString
   *          the ASCII String
   * @throws IOException
   *           on error
   */
  public void writeStringAsciiNullable(OutputStream out, String asciiString) throws IOException;

  /**
   * Write an UTF8 String.
   * 
   * @param out
   *          the output stream
   * @param asciiString
   *          the UTF8 String
   * @throws IOException
   *           on error
   */
  public void writeStringUTF8Nullable(OutputStream out, String utf8String) throws IOException;

  /**
   * Read an ASCII String.
   * @param in the input stream
   * @return the ASCII String
   * @throws IOException on error
   */
  public String readStringAsciiNullable(InputStream in) throws IOException;

  /**
   * Read an UTF8 String.
   * @param in the input stream
   * @return the UTF8 String
   * @throws IOException on error
   */
  public String readStringUTF8Nullable(InputStream in) throws IOException;

  /**
   * Compare version A and version B.
   * @param va the version A
   * @param vb the version B
   * @return version compatible?
   */
  public boolean compareVersions(String va, String vb);
}
