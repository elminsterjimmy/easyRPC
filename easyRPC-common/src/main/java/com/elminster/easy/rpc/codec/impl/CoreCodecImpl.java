package com.elminster.easy.rpc.codec.impl;

import static com.elminster.common.constants.Constants.EncodingConstants.ASCII;
import static com.elminster.common.constants.Constants.EncodingConstants.UTF8;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.exception.IoTimeoutException;
import com.elminster.easy.rpc.exception.ZeroReadException;
import com.elminster.easy.rpc.util.IoUtil;

/**
 * The default implementation of CoreCodec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class CoreCodecImpl implements Codec, Closeable {

  private static long IO_RETRY_INTERVAL = 100; // 100 ms
  private static int IO_RETRY_COUNT_THRESHOLD = 300; // total 30 sec
  
//  private static final Logger logger = LoggerFactory.getLogger(CoreCodecImpl.class);

  /** the IoUtil. */
  private final IoUtil ioUtil;

  /** mark for is null {@literal0}. */
  private static final byte IS_NULL = 0;
  /** mark for not null {@literal1}. */
  private static final byte NOT_NULL = 1;

  private final long timeout;

  public CoreCodecImpl(final IoUtil ioUtil) {
    this.ioUtil = ioUtil;
    this.timeout = IO_RETRY_INTERVAL * IO_RETRY_COUNT_THRESHOLD;
  }

  public CoreCodecImpl(final IoUtil ioUtil, long timeout) {
    this.ioUtil = ioUtil;
    this.timeout = timeout;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeByte(byte value) throws IOException {
    byte[] bytes = new byte[1];
    bytes[0] = value;
    writen(bytes, 0, 1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte readByte() throws IOException {
    byte[] bytes = new byte[1];
    readn(bytes, 0, 1);
    return bytes[0];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeIntBigEndian(int value) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.rewind();
    buffer.putInt(value);
    writen(buffer.array(), 0, 4);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int readIntBigEndian() throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.rewind();
    readn(buffer.array(), 0, buffer.capacity());
    return buffer.getInt();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeLongBigEndian(long longValue) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(8);
    buffer.rewind();
    buffer.putLong(longValue);
    writen(buffer.array(), 0, 8);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long readLongBigEndian() throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(8);
    buffer.rewind();
    readn(buffer.array(), 0, buffer.capacity());
    return buffer.getLong();
  }
  
  public void readn(ByteBuffer buffer) throws IOException {
    if (!buffer.hasRemaining()) {
      return;
    }
    int read = ioUtil.read(buffer);
    if (read < 0) {
      throw new EOFException("Could not read data from closed stream.");
    }
  }
  
  public void writen(ByteBuffer buffer) throws IOException {
    if (!buffer.hasRemaining()) {
      return;
    }
    int written = ioUtil.write(buffer);
    if (written < 0) {
      throw new EOFException("Could not read data from closed stream.");
    } 
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
    int retry = 0;
    while (byteToRead > 0) {
      int curByteRead = 0;
      curByteRead = ioUtil.read(b, curOff, byteToRead);
      if (curByteRead < 0) {
        throw new EOFException("Could not read data from closed stream.");
      } else if (0 == curByteRead) {
        // TODO data not ready, save safepoint
        try {
          if (retry++ > IO_RETRY_COUNT_THRESHOLD) {
            throw new ZeroReadException(String.format("Zero Read exceed retry threshold [%d] in [%s] ms, seems commnunication's broken!", IO_RETRY_COUNT_THRESHOLD,
                IO_RETRY_INTERVAL * IO_RETRY_COUNT_THRESHOLD));
          }
          Thread.sleep(timeout / IO_RETRY_COUNT_THRESHOLD);
        } catch (InterruptedException e) {
          continue;
        }
      } else {
        retry = 0;
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
    writen(encBytes, 0, encSize);
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void writen(byte[] bytes, int off, int len) throws IOException {
    synchronized (Thread.currentThread()) {
      int byteToWrite = len;
      int curOff = off;
      int retry = 0;
      while (byteToWrite > 0) {
        int curByteWritten = 0;
        curByteWritten = ioUtil.write(bytes, curOff, byteToWrite);
        if (curByteWritten < 0) {
          throw new EOFException("Could not write data to closed stream.");
        } else if (0 == curByteWritten) {
          try {
            if (retry++ > IO_RETRY_COUNT_THRESHOLD) {
              throw new IoTimeoutException(String.format("Write exceed retry threshold [%d] in [%s] ms, seems commnunication's broken!", IO_RETRY_COUNT_THRESHOLD,
                  IO_RETRY_INTERVAL * IO_RETRY_COUNT_THRESHOLD));
            }
            Thread.sleep(IO_RETRY_INTERVAL);
          } catch (InterruptedException e) {
            continue;
          }
        }
        byteToWrite -= curByteWritten;
        curOff += curByteWritten;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flush() throws IOException {
    ioUtil.flush();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    this.ioUtil.close();
  }

  @Override
  public String getName() {
    return this.ioUtil.getClass().getName();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ioUtil == null) ? 0 : ioUtil.getClass().getName().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CoreCodecImpl other = (CoreCodecImpl) obj;
    if (ioUtil == null) {
      if (other.ioUtil != null)
        return false;
    } else if (!ioUtil.getClass().getName().equals(other.ioUtil.getClass().getName()))
      return false;
    return true;
  }
}
