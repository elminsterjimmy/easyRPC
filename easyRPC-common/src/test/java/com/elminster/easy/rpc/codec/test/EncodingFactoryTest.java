package com.elminster.easy.rpc.codec.test;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.impl.RpcEncodingFactoryBase;

public class EncodingFactoryTest {

  abstract class InitTemplate {
    public void test() throws Exception {
      RpcEncodingFactory encodingFactory = new RpcEncodingFactoryBase("default");
      try (PipedOutputStream out = new PipedOutputStream(); PipedInputStream in = new PipedInputStream(out)) {
        Codec coreCodec = CoreCodecFactory.INSTANCE.getCoreCodec(in, out);
        encodingFactory.setCodec(coreCodec);
        withTemplate(encodingFactory);
      }

    }

    abstract void withTemplate(RpcEncodingFactory encodingFactory) throws Exception;
  };

  @Test
  public void testWriteAndreadByte() throws Exception {
    new InitTemplate() {

      @Override
      void withTemplate(RpcEncodingFactory encodingFactory) throws Exception {
        byte zero = 0;
        encodingFactory.writeInt8(zero);
        Assert.assertEquals(zero, encodingFactory.readInt8());
        encodingFactory.writeInt8Nullable(Byte.valueOf(zero));
        Assert.assertEquals(Byte.valueOf(zero), encodingFactory.readInt8Nullable());

        byte max = Byte.MAX_VALUE;
        encodingFactory.writeInt8(max);
        Assert.assertEquals(max, encodingFactory.readInt8());
        encodingFactory.writeInt8Nullable(Byte.valueOf(max));
        Assert.assertEquals(Byte.valueOf(max), encodingFactory.readInt8Nullable());

        byte min = Byte.MIN_VALUE;
        encodingFactory.writeInt8(min);
        Assert.assertEquals(min, encodingFactory.readInt8());
        encodingFactory.writeInt8Nullable(Byte.valueOf(min));
        Assert.assertEquals(Byte.valueOf(min), encodingFactory.readInt8Nullable());

        encodingFactory.writeInt8Nullable(null);
        Assert.assertEquals(null, encodingFactory.readInt8Nullable());

      }
    }.test();

  }

  @Test
  public void testWriteAndReadInt() throws Exception {
    new InitTemplate() {

      @Override
      void withTemplate(RpcEncodingFactory encodingFactory) throws Exception {
        int zero = 0;
        encodingFactory.writeInt32(zero);
        Assert.assertEquals(zero, encodingFactory.readInt32());
        encodingFactory.writeInt32Nullable(Integer.valueOf(zero));
        Assert.assertEquals(Integer.valueOf(zero), encodingFactory.readInt32Nullable());

        int max = Integer.MAX_VALUE;
        encodingFactory.writeInt32(max);
        Assert.assertEquals(max, encodingFactory.readInt32());
        encodingFactory.writeInt32Nullable(Integer.valueOf(max));
        Assert.assertEquals(Integer.valueOf(max), encodingFactory.readInt32Nullable());

        int min = Integer.MIN_VALUE;
        encodingFactory.writeInt32(min);
        Assert.assertEquals(min, encodingFactory.readInt32());
        encodingFactory.writeInt32Nullable(Integer.valueOf(min));
        Assert.assertEquals(Integer.valueOf(min), encodingFactory.readInt32Nullable());
        encodingFactory.writeInt32Nullable(Integer.valueOf(min));
        Assert.assertEquals(Integer.valueOf(min), encodingFactory.readInt32Nullable());

        encodingFactory.writeInt32Nullable(null);
        Assert.assertEquals(null, encodingFactory.readInt32Nullable());

      }
    }.test();
  }

  @Test
  public void testWriteAndReadLong() throws Exception {
    new InitTemplate() {

      @Override
      void withTemplate(RpcEncodingFactory encodingFactory) throws Exception {
        long zero = 0L;
        encodingFactory.writeInt64(zero);
        Assert.assertEquals(zero, encodingFactory.readInt64());
        encodingFactory.writeInt64Nullable(Long.valueOf(zero));
        Assert.assertEquals(Long.valueOf(zero), encodingFactory.readInt64Nullable());

        long max = Long.MAX_VALUE;
        encodingFactory.writeInt64(max);
        Assert.assertEquals(max, encodingFactory.readInt64());
        encodingFactory.writeInt64Nullable(Long.valueOf(zero));
        Assert.assertEquals(Long.valueOf(zero), encodingFactory.readInt64Nullable());

        long min = Long.MIN_VALUE;
        encodingFactory.writeInt64(min);
        Assert.assertEquals(min, encodingFactory.readInt64());
        encodingFactory.writeInt64Nullable(Long.valueOf(min));
        Assert.assertEquals(Long.valueOf(min), encodingFactory.readInt64Nullable());

        encodingFactory.writeInt64Nullable(null);
        Assert.assertEquals(null, encodingFactory.readInt64Nullable());
      }
    }.test();
  }

  @Test
  public void testWriteAndReadDouble() throws Exception {
    new InitTemplate() {

      @Override
      void withTemplate(RpcEncodingFactory encodingFactory) throws Exception {
        double zero = 0d;
        encodingFactory.writeDouble(zero);
        Assert.assertEquals(zero, encodingFactory.readDouble(), 0.000001d);
        encodingFactory.writeDoubleNullable(Double.valueOf(zero));
        Assert.assertEquals(Double.valueOf(zero), encodingFactory.readDoubleNullable());

        double max = Double.MAX_VALUE;
        encodingFactory.writeDouble(max);
        Assert.assertEquals(max, encodingFactory.readDouble(), 0.000001d);
        encodingFactory.writeDoubleNullable(Double.valueOf(zero));
        Assert.assertEquals(Double.valueOf(zero), encodingFactory.readDoubleNullable());

        double min = Long.MIN_VALUE;
        encodingFactory.writeDouble(min);
        Assert.assertEquals(min, encodingFactory.readDouble(), 0.000001d);
        encodingFactory.writeDoubleNullable(Double.valueOf(min));
        Assert.assertEquals(Double.valueOf(min), encodingFactory.readDoubleNullable());

        encodingFactory.writeDoubleNullable(null);
        Assert.assertEquals(null, encodingFactory.readDoubleNullable());
      }
    }.test();
  }

  @Test
  public void testWriteAndReadString() throws Exception {
    new InitTemplate() {

      @Override
      void withTemplate(RpcEncodingFactory encodingFactory) throws Exception {
        String nullStr = null;
        encodingFactory.writeAsciiNullable(nullStr);
        Assert.assertEquals(nullStr, encodingFactory.readAsciiNullable());
        encodingFactory.writeStringNullable(nullStr);
        Assert.assertEquals(nullStr, encodingFactory.readStringNullable());

        String emptyString = "";
        encodingFactory.writeAsciiNullable(emptyString);
        Assert.assertEquals(emptyString, encodingFactory.readAsciiNullable());
        encodingFactory.writeStringNullable(emptyString);
        Assert.assertEquals(emptyString, encodingFactory.readStringNullable());

        String asciiString = "abcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()";
        encodingFactory.writeAsciiNullable(asciiString);
        Assert.assertEquals(asciiString, encodingFactory.readAsciiNullable());
        encodingFactory.writeStringNullable(asciiString);
        Assert.assertEquals(asciiString, encodingFactory.readStringNullable());

        String utf8String = "中文 日本語";
        encodingFactory.writeAsciiNullable(utf8String);
        // NOT EQUALS
        Assert.assertNotEquals(utf8String, encodingFactory.readAsciiNullable());
        encodingFactory.writeStringNullable(utf8String);
        Assert.assertEquals(utf8String, encodingFactory.readStringNullable());
      }
    }.test();
  }

  @Test
  public void testWriteAndReadTimestamp() throws Exception {
    new InitTemplate() {

      @Override
      void withTemplate(RpcEncodingFactory encodingFactory) throws Exception {
        encodingFactory.writeObjectNullable(null);
        Assert.assertEquals(null, encodingFactory.readObjectNullable());

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        encodingFactory.writeObjectNullable(ts);
        Assert.assertEquals(ts, encodingFactory.readObjectNullable());
      }
    }.test();
  }

  @Test
  public void testWriteAndReadList() throws Exception {
    new InitTemplate() {

      @Override
      void withTemplate(RpcEncodingFactory encodingFactory) throws Exception {
        List<String> stringList = new LinkedList<String>() {
          /**
           * 
           */
          private static final long serialVersionUID = 1L;

          {
            add("one");
            add("two");
            add("three");
          }
        };
        encodingFactory.writeObjectNullable(stringList);
        Assert.assertEquals(stringList, encodingFactory.readObjectNullable());

        List<String> stringArrayList = new ArrayList<>(stringList);
        encodingFactory.writeObjectNullable(stringArrayList);
        Assert.assertEquals(stringArrayList, encodingFactory.readObjectNullable());

        List<Integer> intList = new LinkedList<Integer>() {
          /**
           * 
           */
          private static final long serialVersionUID = 1L;

          {
            add(1);
            add(2);
            add(3);
          }
        };
        encodingFactory.writeObjectNullable(intList);
        Assert.assertEquals(intList, encodingFactory.readObjectNullable());

        List<Integer> intArrayList = new ArrayList<>(intList);
        encodingFactory.writeObjectNullable(intArrayList);
        Assert.assertEquals(intArrayList, encodingFactory.readObjectNullable());
      }
    }.test();
  }

  @Test
  public void testWriteAndReadSet() throws Exception {
    new InitTemplate() {

      @Override
      void withTemplate(RpcEncodingFactory encodingFactory) throws Exception {
        Set<String> stringSet = new HashSet<String>() {
          /**
           * 
           */
          private static final long serialVersionUID = 1L;

          {
            add("one");
            add("two");
            add("three");
          }
        };
        encodingFactory.writeObjectNullable(stringSet);
        Assert.assertEquals(stringSet, encodingFactory.readObjectNullable());

        Set<String> stringTreeSet = new TreeSet<>(stringSet);
        encodingFactory.writeObjectNullable(stringTreeSet);
        Assert.assertEquals(stringTreeSet, encodingFactory.readObjectNullable());

        Set<Integer> intSet = new HashSet<Integer>() {
          /**
           * 
           */
          private static final long serialVersionUID = 1L;

          {
            add(1);
            add(2);
            add(3);
          }
        };
        encodingFactory.writeObjectNullable(intSet);
        Assert.assertEquals(intSet, encodingFactory.readObjectNullable());

        Set<Integer> intTreeSet = new TreeSet<>(intSet);
        encodingFactory.writeObjectNullable(intTreeSet);
        Assert.assertEquals(intTreeSet, encodingFactory.readObjectNullable());
      }
    }.test();
  }

  @Test
  public void testWriteAndReadMap() throws Exception {
    new InitTemplate() {

      @Override
      void withTemplate(RpcEncodingFactory encodingFactory) throws Exception {
        Map<Integer, String> map = new HashMap<Integer, String>() {
          /**
           * 
           */
          private static final long serialVersionUID = 1L;

          {
            put(1, "one");
            put(2, "two");
            put(3, "three");
          }
        };
        encodingFactory.writeObjectNullable(map);
        Assert.assertEquals(map, encodingFactory.readObjectNullable());

        Map<Integer, String> treeMap = new TreeMap<>(map);
        encodingFactory.writeObjectNullable(treeMap);
        Assert.assertEquals(treeMap, encodingFactory.readObjectNullable());

      }
    }.test();
  }

  @Test
  public void testWriteAndReadArray() throws Exception {
    new InitTemplate() {
      
      @Override
      void withTemplate(RpcEncodingFactory encodingFactory) throws Exception {
        int[] iArr = {1, 2, 3, 4, 5};
        encodingFactory.writeObjectNullable(iArr);
        Assert.assertArrayEquals(iArr, (int[]) encodingFactory.readObjectNullable());
        
        String[] strArr = {"one", "two", "three"};
        encodingFactory.writeObjectNullable(strArr);
        Assert.assertArrayEquals(strArr, (String[]) encodingFactory.readObjectNullable());
      }
    }.test();
  }
  
  
}
