package com.elminster.easy.rpc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcUtilImpl implements RpcUtil {
  
  private static Logger log = LoggerFactory.getLogger(RpcUtilImpl.class);
  
  private static ThreadLocal<ByteBuffer> intBuffer = new ThreadLocal<ByteBuffer>() {
    
    protected ByteBuffer initialValue() {
      return ByteBuffer.allocate(4);
    }
  };
  private static ThreadLocal<ByteBuffer> longBuffer = new ThreadLocal<ByteBuffer>() {
    
    protected ByteBuffer initialValue() {
      return ByteBuffer.allocate(8);
    }
  };
  
  private static final int IS_NULL = 0;
  private static final int NOT_NULL = 1;

  public void writeByte(OutputStream oStream, byte value) throws IOException {
    oStream.write(value);
  }

  public byte readByte(InputStream iStream) throws IOException {
    int value = iStream.read();
    if (value < 0) {
      throw new IOException("Could not decode data from closed stream");
    }
    return (byte) value;
  }

  public void writeIntBigEndian(OutputStream oStream, int value) throws IOException {
    intBuffer.get().rewind();
    intBuffer.get().putInt(value);
    oStream.write(intBuffer.get().array());
  }

  public int readIntBigEndian(InputStream iStream) throws IOException {
    intBuffer.get().rewind();
    readn(iStream, intBuffer.get().array(), 0, intBuffer.get().capacity());
    return intBuffer.get().getInt();
  }

  public void writeLongBigEndian(OutputStream oStream, long value) throws IOException {
    longBuffer.get().rewind();
    longBuffer.get().putLong(value);
    oStream.write(longBuffer.get().array());
  }

  public long readLongBigEndian(InputStream iStream) throws IOException {
    longBuffer.get().rewind();
    readn(iStream, longBuffer.get().array(), 0, longBuffer.get().capacity());
    return longBuffer.get().getLong();
  }

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

  public void writeStringAsciiNullable(OutputStream oStream, String stringValue) throws IOException {
    writeStringNullable(oStream, stringValue, "US-ASCII");
  }

  public void writeStringUTF8Nullable(OutputStream oStream, String stringValue) throws IOException {
    writeStringNullable(oStream, stringValue, "UTF-8");
  }

  public void writeStringAscii(OutputStream oStream, String stringValue) throws IOException {
    writeString(oStream, stringValue, "US-ASCII");
  }

  public void writeStringUTF8(OutputStream oStream, String stringValue) throws IOException {
    writeString(oStream, stringValue, "UTF-8");
  }

  private void writeStringNullable(OutputStream oStream, String stringValue, String encoding) throws IOException {
    if (stringValue == null) {
      oStream.write(IS_NULL);
      return;
    }
    oStream.write(NOT_NULL);
    writeString(oStream, stringValue, encoding);
  }

  private void writeString(OutputStream oStream, String stringValue, String encoding) throws IOException {
    byte[] encBytes = stringValue.getBytes(encoding);
    int encSize = encBytes.length;
    writeIntBigEndian(oStream, encSize);
    oStream.write(encBytes, 0, encSize);
  }

  public String readStringAsciiNullable(InputStream iStream) throws IOException {
    return readStringNullable(iStream, "US-ASCII");
  }

  public String readStringUTF8Nullable(InputStream iStream) throws IOException {
    return readStringNullable(iStream, "UTF-8");
  }

  public String readStringAscii(InputStream iStream) throws IOException {
    return readString(iStream, "US-ASCII");
  }

  public String readStringUTF8(InputStream iStream) throws IOException {
    return readString(iStream, "UTF-8");
  }

  private String readStringNullable(InputStream iStream, String encoding) throws IOException {
    byte isNull = readByte(iStream);
    if (isNull == IS_NULL) {
      return null;
    }
    return readString(iStream, encoding);
  }

  private String readString(InputStream iStream, String encoding) throws IOException {
    int len = readIntBigEndian(iStream);
    byte[] encodingBytes = new byte[len];

    readn(iStream, encodingBytes, 0, len);
    Charset cs = Charset.forName(encoding);
    CharBuffer cb = cs.decode(ByteBuffer.wrap(encodingBytes));
    return cb.toString();
  }

  public boolean compareVersions(String va, String vb) {
    if (log.isDebugEnabled()) {
      log.debug("Version A = " + va + "; version B = " + vb);
    }
    if ((va == null) || (vb == null)) {
      return false;
    }
    if (va.equals(vb)) {
      return true;
    }
    String[] arrA = va.split("\\.");
    String[] arrB = vb.split("\\.");
    int length = Math.min(arrA.length, arrB.length);
    if (length < 2) {
      log.error("Incorrect version format [" + va + "; " + vb + "]. Required: x.y.*");
      return false;
    }
    return (arrA[0] + arrA[1]).equals(arrB[0] + arrB[1]);
  }
}
