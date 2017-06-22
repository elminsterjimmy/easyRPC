package com.elminster.easy.rpc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.elminster.common.constants.Constants.EncodingConstants.UTF8;
import static com.elminster.common.constants.Constants.EncodingConstants.ASCII;
import static com.elminster.common.constants.RegexConstants.REGEX_DOT;

/**
 * The default implementation of RpcUtil.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcUtilImpl implements RpcUtil {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(RpcUtilImpl.class);

  /** shared int buffer. */
  private static ThreadLocal<ByteBuffer> intBuffer = new ThreadLocal<ByteBuffer>() {

    protected ByteBuffer initialValue() {
      return ByteBuffer.allocate(4);
    }
  };

  /** shared long buffer. */
  private static ThreadLocal<ByteBuffer> longBuffer = new ThreadLocal<ByteBuffer>() {

    protected ByteBuffer initialValue() {
      return ByteBuffer.allocate(8);
    }
  };

  /** mark for is null {@literal0}. */
  private static final byte IS_NULL = 0;
  /** mark for not null {@literal1}. */
  private static final byte NOT_NULL = 1;

  /**
   * {@inheritDoc}
   */
  public void writeByte(OutputStream oStream, byte value) throws IOException {
    oStream.write(value);
  }

  /**
   * {@inheritDoc}
   */
  public byte readByte(InputStream iStream) throws IOException {
    int value = iStream.read();
    if (value < 0) {
      throw new IOException("Could not decode data from closed stream");
    }
    return (byte) value;
  }

  /**
   * {@inheritDoc}
   */
  public void writeIntBigEndian(OutputStream oStream, int value) throws IOException {
    intBuffer.get().rewind();
    intBuffer.get().putInt(value);
    oStream.write(intBuffer.get().array());
  }

  /**
   * {@inheritDoc}
   */
  public int readIntBigEndian(InputStream iStream) throws IOException {
    intBuffer.get().rewind();
    readn(iStream, intBuffer.get().array(), 0, intBuffer.get().capacity());
    return intBuffer.get().getInt();
  }

  /**
   * {@inheritDoc}
   */
  public void writeLongBigEndian(OutputStream oStream, long value) throws IOException {
    longBuffer.get().rewind();
    longBuffer.get().putLong(value);
    oStream.write(longBuffer.get().array());
  }

  /**
   * {@inheritDoc}
   */
  public long readLongBigEndian(InputStream iStream) throws IOException {
    longBuffer.get().rewind();
    readn(iStream, longBuffer.get().array(), 0, longBuffer.get().capacity());
    return longBuffer.get().getLong();
  }

  /**
   * {@inheritDoc}
   */
  public void readn(InputStream iStream, byte[] b, int off, int len) throws IOException {
    if (len == 0) {
      return;
    }
    int byteToRead = len;
    int curOff = off;
    while (byteToRead > 0) {
      int curByteRead = 0;
      curByteRead = iStream.read(b, curOff, byteToRead);
      if (curByteRead < 0) {
        throw new IOException("Could not read data from closed stream");
      }
      byteToRead -= curByteRead;
      curOff += curByteRead;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void writeStringAsciiNullable(OutputStream oStream, String stringValue) throws IOException {
    writeStringNullable(oStream, stringValue, ASCII);
  }

  /**
   * {@inheritDoc}
   */
  public void writeStringUTF8Nullable(OutputStream oStream, String stringValue) throws IOException {
    writeStringNullable(oStream, stringValue, UTF8);
  }

  /**
   * Write a nullable String.
   * 
   * @param oStream
   *          the output stream
   * @param stringValue
   *          the String value
   * @param encoding
   *          the String encoding
   * @throws IOException
   *           on error
   */
  private void writeStringNullable(OutputStream oStream, String stringValue, String encoding) throws IOException {
    if (stringValue == null) {
      oStream.write(IS_NULL);
      return;
    }
    oStream.write(NOT_NULL);
    writeString(oStream, stringValue, encoding);
  }

  /**
   * Write a String.
   * 
   * @param oStream
   *          the output stream
   * @param stringValue
   *          the String value
   * @param encoding
   *          the String encoding
   * @throws IOException
   *           on error
   */
  private void writeString(OutputStream oStream, String stringValue, String encoding) throws IOException {
    byte[] encBytes = stringValue.getBytes(encoding);
    int encSize = encBytes.length;
    writeIntBigEndian(oStream, encSize);
    oStream.write(encBytes, 0, encSize);
  }

  /**
   * {@inheritDoc}
   */
  public String readStringAsciiNullable(InputStream iStream) throws IOException {
    return readStringNullable(iStream, ASCII);
  }

  /**
   * {@inheritDoc}
   */
  public String readStringUTF8Nullable(InputStream iStream) throws IOException {
    return readStringNullable(iStream, UTF8);
  }

  /**
   * Read a nullable String.
   * 
   * @param iStream
   *          the input stream
   * @param encoding
   *          the String encoding
   * @return the String
   * @throws IOException
   *           on error
   */
  private String readStringNullable(InputStream iStream, String encoding) throws IOException {
    byte isNull = readByte(iStream);
    if (isNull == IS_NULL) {
      return null;
    }
    return readString(iStream, encoding);
  }

  /**
   * Read a String.
   * 
   * @param iStream
   *          the input stream
   * @param encoding
   *          the String encoding
   * @return the String
   * @throws IOException
   *           on error
   */
  private String readString(InputStream iStream, String encoding) throws IOException {
    int len = readIntBigEndian(iStream);
    byte[] encodingBytes = new byte[len];

    readn(iStream, encodingBytes, 0, len);
    Charset cs = Charset.forName(encoding);
    CharBuffer cb = cs.decode(ByteBuffer.wrap(encodingBytes));
    return cb.toString();
  }

  /**
   * {@inheritDoc}
   */
  public boolean compareVersions(String va, String vb) {
    if (logger.isDebugEnabled()) {
      logger.debug("Version A = " + va + "; version B = " + vb);
    }
    if ((va == null) || (vb == null)) {
      return false;
    }
    if (va.equals(vb)) {
      return true;
    }
    String[] arrA = va.split(REGEX_DOT);
    String[] arrB = vb.split(REGEX_DOT);
    int length = Math.min(arrA.length, arrB.length);
    if (length < 2) {
      logger.error("Incorrect version format [" + va + "; " + vb + "]. Required: x.y.*");
      return false;
    }
    return (arrA[0] + arrA[1]).equals(arrB[0] + arrB[1]);
  }
}
