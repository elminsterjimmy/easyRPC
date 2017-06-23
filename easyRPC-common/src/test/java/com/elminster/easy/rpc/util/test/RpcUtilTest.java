package com.elminster.easy.rpc.util.test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.elminster.easy.rpc.util.RpcUtil;
import com.elminster.easy.rpc.util.RpcUtilImpl;

public class RpcUtilTest {
  
  RpcUtil util = new RpcUtilImpl();

  @Test
  public void testWriteAndReadByte() throws IOException {
    try (PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out)) {
      byte zero = 0;
      util.writeByte(out, zero);
      Assert.assertEquals(zero, util.readByte(in));
      
      byte max = Byte.MAX_VALUE;
      util.writeByte(out, max);
      Assert.assertEquals(max, util.readByte(in));
      
      byte min = Byte.MIN_VALUE;
      util.writeByte(out, min);
      Assert.assertEquals(min, util.readByte(in));
    }
  }
  
  @Test
  public void testWriteAndReadInt() throws IOException {
    try (PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out)) {
      int zero = 0;
      util.writeIntBigEndian(out, zero);
      Assert.assertEquals(zero, util.readIntBigEndian(in));
      
      int max = Integer.MAX_VALUE;
      util.writeIntBigEndian(out, max);
      Assert.assertEquals(max, util.readIntBigEndian(in));
      
      int min = Integer.MIN_VALUE;
      util.writeIntBigEndian(out, min);
      Assert.assertEquals(min, util.readIntBigEndian(in));
    }
  }
  
  @Test
  public void testWriteAndReadLong() throws IOException {
    try (PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out)) {
      long zero = 0L;
      util.writeLongBigEndian(out, zero);
      Assert.assertEquals(zero, util.readLongBigEndian(in));
      
      long max = Long.MAX_VALUE;
      util.writeLongBigEndian(out, max);
      Assert.assertEquals(max, util.readLongBigEndian(in));
      
      long min = Long.MIN_VALUE;
      util.writeLongBigEndian(out, min);
      Assert.assertEquals(min, util.readLongBigEndian(in));
    }
  }

  @Test
  public void testWriteAndReadString() throws IOException {
    try (PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out)) {
      String nullStr = null;
      util.writeStringAsciiNullable(out, nullStr);
      Assert.assertEquals(nullStr, util.readStringAsciiNullable(in));
      util.writeStringUTF8Nullable(out, nullStr);
      Assert.assertEquals(nullStr, util.readStringUTF8Nullable(in));
      
      String emptyString = "";
      util.writeStringAsciiNullable(out, emptyString);
      Assert.assertEquals(emptyString, util.readStringAsciiNullable(in));
      util.writeStringUTF8Nullable(out, emptyString);
      Assert.assertEquals(emptyString, util.readStringUTF8Nullable(in));
      
      String asciiString = "abcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()";
      util.writeStringAsciiNullable(out, asciiString);
      Assert.assertEquals(asciiString, util.readStringAsciiNullable(in));
      util.writeStringUTF8Nullable(out, asciiString);
      Assert.assertEquals(asciiString, util.readStringUTF8Nullable(in));
      
      String utf8String = "中文 日本語";
      util.writeStringAsciiNullable(out, utf8String);
      // NOT EQUALS
      Assert.assertNotEquals(utf8String, util.readStringAsciiNullable(in));
      util.writeStringUTF8Nullable(out, utf8String);
      Assert.assertEquals(utf8String, util.readStringUTF8Nullable(in));
    }
  }
}
