package com.elminster.easy.rpc.idl.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.impl.BooleanCodec;
import com.elminster.easy.rpc.codec.impl.ByteCodec;
import com.elminster.easy.rpc.codec.impl.DoubleCodec;
import com.elminster.easy.rpc.codec.impl.HashSetCodec;
import com.elminster.easy.rpc.codec.impl.IntegerCodec;
import com.elminster.easy.rpc.codec.impl.ListCodec;
import com.elminster.easy.rpc.codec.impl.LongCodec;
import com.elminster.easy.rpc.codec.impl.MapCodec;
import com.elminster.easy.rpc.codec.impl.ObjectCodec;
import com.elminster.easy.rpc.codec.impl.StringCodec;
import com.elminster.easy.rpc.codec.impl.TimestampCodec;
import com.elminster.easy.rpc.idl.IDL;

public enum IDLBasicTypes implements IDL {

  // @formatter:off
  B(Byte.TYPE.getCanonicalName(), "b", Byte.TYPE, ByteCodec.class),
  I(Integer.TYPE.getCanonicalName(), "i", Integer.TYPE, IntegerCodec.class),
  J(Long.TYPE.getCanonicalName(), "j", Long.TYPE, LongCodec.class),
  D(Double.TYPE.getCanonicalName(), "d", Double.TYPE, DoubleCodec.class),
  Z(Boolean.TYPE.getCanonicalName(), "z", Boolean.TYPE, BooleanCodec.class),
  BYTE(Byte.class.getCanonicalName(), "B", Byte.class, ByteCodec.class),
  INTEGER(Integer.class.getCanonicalName(), "I", Integer.class, IntegerCodec.class),
  LONG(Long.class.getCanonicalName(), "J", Long.class, LongCodec.class),
  DOUBLE(Double.class.getCanonicalName(), "D", Double.class, DoubleCodec.class),
  BOOLEAN(Boolean.class.getCanonicalName(), "Z", Boolean.class, BooleanCodec.class),
  STRING(String.class.getCanonicalName(), "S", String.class, StringCodec.class),
  OBJECT(Object.class.getCanonicalName(), "O", Object.class, ObjectCodec.class),
  TIMESTAMP(Timestamp.class.getCanonicalName(), "T", Timestamp.class, TimestampCodec.class),
  LINKEDLIST(LinkedList.class.getCanonicalName(), "L", LinkedList.class, ListCodec.class),
  HASHMAP(HashMap.class.getCanonicalName(), "M", HashMap.class, MapCodec.class),
  HASHSET(HashSet.class.getCanonicalName(), "E", HashSet.class, HashSetCodec.class);
  // @formatter:on

  private final String localName;
  private final String remoteName;
  private final Class<?> clazz;
  private final Class<? extends RpcCodec> codec;

  // TODO
  private IDLBasicTypes(String local, String remote, Class<?> typeClass, Class<? extends RpcCodec> codecClass) {
    this.localName = local;
    this.remoteName = remote;
    this.clazz = typeClass;
    this.codec = codecClass;
  }

  public static IDL getByRemoteName(String name) {
    for (IDLBasicTypes e : IDLBasicTypes.values()) {
      if (e.getRemoteName().equals(name)) {
        return e;
      }
    }
    return null;
  }

  public static IDL getByName(String name) {
    for (IDLBasicTypes e : IDLBasicTypes.values()) {
      if (e.name().equals(name)) {
        return e;
      }
    }
    return null;
  }

  public String getLocalName() {
    return this.localName;
  }

  public String getRemoteName() {
    return this.remoteName;
  }

  public Class<?> getTypeClass() {
    return this.clazz;
  }

  public Class<? extends RpcCodec> getCodecClass() {
    return this.codec;
  }

}
