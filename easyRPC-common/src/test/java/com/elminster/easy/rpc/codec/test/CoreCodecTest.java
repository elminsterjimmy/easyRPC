package com.elminster.easy.rpc.util.test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.elminster.easy.rpc.util.RpcUtil;
import com.elminster.easy.rpc.util.RpcUtilFactory;

public class RpcUtilTest {
  

  @Test
  public void testWriteAndReadByte() throws IOException {
    try (PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out)) {
      
      RpcUtil util = RpcUtilFactory.INSTANCE.getRpcUtil(in, out);
      byte zero = 0;
      util.writeByte(zero);
      Assert.assertEquals(zero, util.readByte());
      
      byte max = Byte.MAX_VALUE;
      util.writeByte(max);
      Assert.assertEquals(max, util.readByte());
      
      byte min = Byte.MIN_VALUE;
      util.writeByte(min);
      Assert.assertEquals(min, util.readByte());
    }
  }
  
  @Test
  public void testWriteAndReadInt() throws IOException {
    try (PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out)) {
      RpcUtil util = RpcUtilFactory.INSTANCE.getRpcUtil(in, out);
      
      int zero = 0;
      util.writeIntBigEndian(zero);
      Assert.assertEquals(zero, util.readIntBigEndian());
      
      int max = Integer.MAX_VALUE;
      util.writeIntBigEndian(max);
      Assert.assertEquals(max, util.readIntBigEndian());
      
      int min = Integer.MIN_VALUE;
      util.writeIntBigEndian(min);
      Assert.assertEquals(min, util.readIntBigEndian());
    }
  }
  
  @Test
  public void testWriteAndReadLong() throws IOException {
    try (PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out)) {
      
      RpcUtil util = RpcUtilFactory.INSTANCE.getRpcUtil(in, out);
      long zero = 0L;
      util.writeLongBigEndian(zero);
      Assert.assertEquals(zero, util.readLongBigEndian());
      
      long max = Long.MAX_VALUE;
      util.writeLongBigEndian(max);
      Assert.assertEquals(max, util.readLongBigEndian());
      
      long min = Long.MIN_VALUE;
      util.writeLongBigEndian(min);
      Assert.assertEquals(min, util.readLongBigEndian());
    }
  }

  @Test
  public void testWriteAndReadString() throws IOException {
    try (PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out)) {
      
      RpcUtil util = RpcUtilFactory.INSTANCE.getRpcUtil(in, out);
      String nullStr = null;
      util.writeStringAsciiNullable(nullStr);
      Assert.assertEquals(nullStr, util.readStringAsciiNullable());
      util.writeStringUTF8Nullable(nullStr);
      Assert.assertEquals(nullStr, util.readStringUTF8Nullable());
      
      String emptyString = "";
      util.writeStringAsciiNullable(emptyString);
      Assert.assertEquals(emptyString, util.readStringAsciiNullable());
      util.writeStringUTF8Nullable(emptyString);
      Assert.assertEquals(emptyString, util.readStringUTF8Nullable());
      
      String asciiString = "abcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()";
      util.writeStringAsciiNullable(asciiString);
      Assert.assertEquals(asciiString, util.readStringAsciiNullable());
      util.writeStringUTF8Nullable(asciiString);
      Assert.assertEquals(asciiString, util.readStringUTF8Nullable());
      
      String utf8String = "中文 日本語";
      util.writeStringAsciiNullable(utf8String);
      // NOT EQUALS
      Assert.assertNotEquals(utf8String, util.readStringAsciiNullable());
      util.writeStringUTF8Nullable(utf8String);
      Assert.assertEquals(utf8String, util.readStringUTF8Nullable());
    }
  }
}
