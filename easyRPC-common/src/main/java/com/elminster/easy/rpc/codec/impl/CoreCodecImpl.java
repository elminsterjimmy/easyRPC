package com.elminster.easy.rpc.codec.impl;

import static com.elminster.common.constants.Constants.EncodingConstants.ASCII;
import static com.elminster.common.constants.Constants.EncodingConstants.UTF8;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import com.elminster.easy.rpc.codec.CoreCodec;
import com.elminster.easy.rpc.util.IoUtil;

/**
 * The default implementation of CoreCodec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class CoreCodecImpl implements CoreCodec {

  /** shared byte buffer. */
  private static ThreadLocal<ByteBuffer> byteBuffer = new ThreadLocal<ByteBuffer>() {

    protected ByteBuffer initialValue() {
      return ByteBuffer.allocate(1);
    }
  };

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
  
  /** the IoUtil. */
  private final IoUtil ioUtil;

  /** mark for is null {@literal0}. */
  private static final byte IS_NULL = 0;
  /** mark for not null {@literal1}. */
  private static final byte NOT_NULL = 1;
  
  public CoreCodecImpl(final IoUtil ioUtil) {
    this.ioUtil = ioUtil;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void writeByte(byte value) throws IOException {
    byteBuffer.get().rewind();
    byteBuffer.get().put(value);
    ioUtil.write(byteBuffer.get().array(), 0, 1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte readByte() throws IOException {
    byteBuffer.get().rewind();
    readn(byteBuffer.get().array(), 0, byteBuffer.get().capacity());
    return byteBuffer.get().get();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeIntBigEndian(int value) throws IOException {
    intBuffer.get().rewind();
    intBuffer.get().putInt(value);
    ioUtil.write(intBuffer.get().array(), 0, 4);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int readIntBigEndian() throws IOException {
    intBuffer.get().rewind();
    readn(intBuffer.get().array(), 0, intBuffer.get().capacity());
    return intBuffer.get().getInt();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeLongBigEndian(long longValue) throws IOException {
    longBuffer.get().rewind();
    longBuffer.get().putLong(longValue);
    ioUtil.write(longBuffer.get().array(), 0, 8);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long readLongBigEndian() throws IOException {
    longBuffer.get().rewind();
    readn(longBuffer.get().array(), 0, longBuffer.get().capacity());
    return longBuffer.get().getLong();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void readn(byte[] b, int off, int len) throws IOException {
    if (len <= 0) {
      return;
    }
    int byteToRead = len;
    int curOff = off;
    while (byteToRead > 0) {
      int curByteRead = 0;
      curByteRead = ioUtil.read(b, curOff, byteToRead);
      if (curByteRead < 0) {
        throw new EOFException("Could not read data from closed stream");
      }
      byteToRead -= curByteRead;
      curOff += curByteRead;
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void writeStringAsciiNullable(String stringValue) throws IOException {
    writeStringNullable(stringValue, ASCII);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeStringUTF8Nullable(String stringValue) throws IOException {
    writeStringNullable(stringValue, UTF8);
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
  private void writeStringNullable(String stringValue, String encoding) throws IOException {
    if (stringValue == null) {
      writeByte(IS_NULL);
      return;
    }
    writeByte(NOT_NULL);
    writeString(stringValue, encoding);
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
  private void writeString(String stringValue, String encoding) throws IOException {
    byte[] encBytes = stringValue.getBytes(encoding);
    int encSize = encBytes.length;
    writeIntBigEndian(encSize);
    ioUtil.write(encBytes, 0, encSize);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String readStringAsciiNullable() throws IOException {
    return readStringNullable(ASCII);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String readStringUTF8Nullable() throws IOException {
    return readStringNullable(UTF8);
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
  private String readStringNullable(String encoding) throws IOException {
    byte isNull = readByte();
    if (isNull == IS_NULL) {
      return null;
    }
    return readString(encoding);
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
  private String readString(String encoding) throws IOException {
    int len = readIntBigEndian();
    byte[] encodingBytes = new byte[len];

    readn(encodingBytes, 0, len);
    Charset cs = Charset.forName(encoding);
    CharBuffer cb = cs.decode(ByteBuffer.wrap(encodingBytes));
    return cb.toString();
  }
}
