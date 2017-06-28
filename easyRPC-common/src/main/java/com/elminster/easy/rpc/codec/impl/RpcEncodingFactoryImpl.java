package com.elminster.easy.rpc.codec.impl;

import static com.elminster.easy.rpc.codec.CodecConst.IS_NULL;
import static com.elminster.easy.rpc.codec.CodecConst.NOT_NULL;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.constants.Constants.StringConstants;
import com.elminster.common.util.Assert;
import com.elminster.common.util.StringUtil;
import com.elminster.easy.rpc.codec.CodecRepository;
import com.elminster.easy.rpc.codec.CoreCodec;
import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.compressor.DataCompressor;
import com.elminster.easy.rpc.compressor.DataCompressorFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.idl.IDL;
import com.elminster.easy.rpc.idl.impl.IDLBasicTypes;

/**
 * The RPC encoding factory implementation.
 * 
 * @author jinggu
 * @version 1.0
 */
public abstract class RpcEncodingFactoryImpl implements RpcEncodingFactory {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(RpcEncodingFactoryImpl.class);

  /** {@literal ^\\[*} */
  private static final Pattern REPLACE_ARRAY_PREFIX_PATTERN = Pattern.compile("^\\[*");
  /** {@literal ^L } */
  private static final Pattern REPLACE_CLASS_PREFIX_PATTERN = Pattern.compile("^L");
  /** {@literal ; } */
  private static final Pattern REPLACE_CLASS_SUFFIX_PATTERN = Pattern.compile(";");

  private final String encodingName;
  /** class name -> remote type name. */
  protected HashMap<String, String> classNameToRemoteTypeNameMap = new HashMap<>();
  /** remote type name -> class name. */
  protected HashMap<String, String> remoteTypeNameToClassNameMap = new HashMap<>();
  /** class name -> codec class. */
  protected HashMap<String, Class<? extends RpcCodec>> encodingClassMap = new HashMap<>();
  /** class name -> codec instance. */
  protected HashMap<String, RpcCodec> encodingInstanceMap = new HashMap<>();

  private CoreCodec rpcUtil;
  private DataCompressorFactory compressorFactory;
  
  public RpcEncodingFactoryImpl(String encodingName) {
    this.encodingName = encodingName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getEncodingName() {
    return this.encodingName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addEncodingClass(final Class<?> clazz, final Class<? extends RpcCodec> encClass, final String remoteName) {
    addEncodingClass(clazz.getName(), encClass, remoteName);
  }

  /**
   * {@inheritDoc}
   */
  @Override

  public void addEncodingClass(final String className, final Class<? extends RpcCodec> encClass, final String remoteName) {
    this.encodingClassMap.put(className, encClass);
    setRemoteForClassName(className, remoteName);
  }

  /**
   * {@inheritDoc}
   */
  @Override

  public void addEncodingInstance(final Class<?> clazz, final RpcCodec encObject, final String remoteName) {
    addEncodingInstance(clazz.getName(), encObject, remoteName);
  }

  /**
   * {@inheritDoc}
   */
  @Override

  public void addEncodingInstance(final String className, final RpcCodec encObject, final String remoteName) {
    this.encodingInstanceMap.put(className, encObject);
    setRemoteForClassName(className, remoteName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcCodec getEncodingObject(final Class<?> clazz) throws RpcException {
    if (null == clazz) {
      return null;
    }
    RpcCodec codec = getEncodingObject(clazz.getName(), TypeCategory.JAVA);
    if (null == codec) {
      Class<?> parentClazz = getParentClassForEncoding(clazz);
      if (null != parentClazz) {
        codec = getEncodingObject(parentClazz);
        addEncodingInstance(clazz, codec, getRemoteNameForClassName(parentClazz.getName()));
      }
    }
    if (null == codec) {
      throw new RpcException("No codec registered for class" + clazz.getName());
    }
    return codec;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcCodec getEncodingObject(final String typeName, final TypeCategory typeCategory) throws RpcException {
    RpcCodec codec = getEncodingObjectInternal(typeName, typeCategory);
    return codec;
  }

  /**
   * Get the Codec for specified type.
   * 
   * @param typeName
   *          the type name
   * @param typeCategory
   *          the type category
   * @return the codec corresponded to the class
   * @throws RpcException
   *           on error
   */
  private RpcCodec getEncodingObjectInternal(String typeName, TypeCategory typeCategory) throws RpcException {
    if (null == typeName) {
      return null;
    }
    String paramClassName = null;
    try {
      RpcCodec codec = null;

      boolean isArray = false;
      switch (typeCategory) {
      case IDL:
        isArray = typeName.charAt(typeName.length() - 1) == ']'; // xxx[]
        break;
      case JAVA:
        isArray = typeName.charAt(0) == '['; // [I
        break;
      }
      if (isArray) {
        codec = getDefaultArrayCodec();
        if (TypeCategory.JAVA == typeCategory) {
          String cName = getCanonicalName(typeName);
          String remoteTypeName = this.classNameToRemoteTypeNameMap.get(cName);
          this.classNameToRemoteTypeNameMap.put(typeName, remoteTypeName + "[]"); // FIXME only 1 dimension array?
        }
      }
      if (null == codec) {
        if (TypeCategory.IDL == typeCategory) {
          paramClassName = this.remoteTypeNameToClassNameMap.get(typeName);
          if (null == paramClassName) {
            return null;
          }
        } else {
          paramClassName = typeName;
        }
        codec = this.encodingInstanceMap.get(paramClassName);
        if (null == codec) {
          Class<? extends RpcCodec> encInstanceClass = this.encodingClassMap.get(paramClassName);
          if (null != encInstanceClass) {
            codec = encInstanceClass.newInstance();
          }
        }
      }
      return codec;
    } catch (Exception e) {
      logger.error(e.toString(), e);
      throw new RpcException("Could not instantiate Encoder for " + paramClassName);
    }
  }

  /**
   * Get the canonical name of the type name.
   * 
   * @param typeName
   *          the type name
   * @return the canonical name
   */
  private String getCanonicalName(String typeName) {
    if (StringUtil.isEmpty(typeName)) {
      return null;
    }
    String result = null;

    String cn = REPLACE_ARRAY_PREFIX_PATTERN.matcher(typeName).replaceAll(StringConstants.EMPTY_STRING);
    cn = REPLACE_CLASS_PREFIX_PATTERN.matcher(cn).replaceFirst(StringConstants.EMPTY_STRING);
    cn = REPLACE_CLASS_SUFFIX_PATTERN.matcher(cn).replaceAll(StringConstants.EMPTY_STRING);

    IDL bt = IDLBasicTypes.getByName(cn);
    if (bt != null) {
      result = bt.getLocalName();
    } else {
      result = cn;
    }
    return result;
  }

  /**
   * Get parent class for encoding.
   * 
   * @param clazz
   *          the class
   * @return the parent class that encoding object is available otherwise {@literal null}
   */
  protected Class<?> getParentClassForEncoding(Class<?> clazz) {
    try {
      Class<?> parentClazz = clazz.getSuperclass();
      while (null != parentClazz) {
        if ((getEncodingObject(parentClazz) != null) && (!parentClazz.equals(Object.class))) {
          return parentClazz;
        }
        parentClazz = parentClazz.getSuperclass();
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }

  /**
   * Get the default array codec.
   * 
   * @return the default array codec
   */
  protected RpcCodec getDefaultArrayCodec() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getClassNameForRemoteName(String remoteTypeName) {
    return this.remoteTypeNameToClassNameMap.get(remoteTypeName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getRemoteNameForClassName(final String className) {
    return this.classNameToRemoteTypeNameMap.get(className);
  }

  public void setRemoteForClassName(final String className, final String remoteName) {
    this.classNameToRemoteTypeNameMap.put(className, remoteName);
    this.remoteTypeNameToClassNameMap.put(remoteName, className);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getRegisteredClassNames() {
    Set<String> encodingClassNames = encodingClassMap.keySet();
    Set<String> encodingInstanceClassNames = encodingInstanceMap.keySet();
    Set<String> set = new HashSet<>(encodingClassNames.size() + encodingInstanceClassNames.size());
    set.addAll(encodingClassNames);
    set.addAll(encodingClassNames);
    return Collections.unmodifiableSet(set);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getRegisteredRemoteNames() {
    return Collections.unmodifiableSet(remoteTypeNameToClassNameMap.keySet());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean readIsNotNull() throws IOException {
    return NOT_NULL == rpcUtil.readByte();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeIsNotNull(final boolean isNotNull) throws IOException {
    if (isNotNull) {
      rpcUtil.writeByte(NOT_NULL);
    } else {
      rpcUtil.writeByte(IS_NULL);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object readObjectNullable() throws IOException, RpcException {
    Object result = null;
    if (readIsNotNull()) {
      String aliasName = rpcUtil.readStringAsciiNullable();

      RpcCodec codec = getEncodingObject(aliasName, TypeCategory.IDL);
      if (null == codec) {
        throw new RpcException("No codec registered for remote type " + aliasName);
      }
      result = codec.decode(this);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeObjectNullable(final Object value) throws IOException, RpcException {
    writeIsNotNull(null != value);
    if (null == value) {
      return;
    }
    RpcCodec codec = null;
    try {
      codec = getEncodingObject(value.getClass());
    } catch (RpcException e) {
      if ((value instanceof Throwable)) {
        logger.error("No codec registered: " + value.getClass() + " call stack ", (Throwable) value);
      }
      throw e;
    }
    encodeClassName(value.getClass());
    codec.encode(value, this);
  }

  /**
   * Encode the class name.
   * 
   * @param out
   *          the output stream
   * @param clazz
   *          the class
   * @throws IOException
   *           on error
   */
  private void encodeClassName(final Class<?> clazz) throws IOException {
    String className = clazz.getName();
    String remoteTypeName = getRemoteNameForClassName(className);
    rpcUtil.writeStringAsciiNullable(remoteTypeName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long readInt64Nullable() throws IOException, RpcException {
    if (!readIsNotNull()) {
      return null;
    }
    return Long.valueOf(rpcUtil.readLongBigEndian());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeInt64Nullable(final Long value) throws IOException, RpcException {
    if (null != value) {
      writeIsNotNull(true);
      rpcUtil.writeLongBigEndian(value.longValue());
    } else {
      writeIsNotNull(false);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Byte readInt8Nullable() throws IOException, RpcException {
    if (!readIsNotNull()) {
      return null;
    }
    return Byte.valueOf(rpcUtil.readByte());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeInt8Nullable(final Byte value) throws IOException, RpcException {
    if (null != value) {
      writeIsNotNull(true);
      rpcUtil.writeByte(value.byteValue());
    } else {
      writeIsNotNull(false);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Integer readInt32Nullable() throws IOException, RpcException {
    if (!readIsNotNull()) {
      return null;
    }
    return Integer.valueOf(rpcUtil.readIntBigEndian());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeInt32Nullable(final Integer data) throws IOException, RpcException {
    if (data != null) {
      writeIsNotNull(true);
      rpcUtil.writeIntBigEndian(data.intValue());
    } else {
      writeIsNotNull(false);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String readStringNullable() throws IOException, RpcException {
    return rpcUtil.readStringUTF8Nullable();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeStringNullable(final String value) throws IOException, RpcException {
    rpcUtil.writeStringUTF8Nullable(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String readAsciiNullable() throws IOException, RpcException {
    return rpcUtil.readStringAsciiNullable();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeAsciiNullable(String value) throws IOException, RpcException {
    rpcUtil.writeStringAsciiNullable(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double readDoubleNullable() throws IOException, RpcException {
    if (!readIsNotNull()) {
      return null;
    }
    long bits = rpcUtil.readLongBigEndian();
    double curDouble = Double.longBitsToDouble(bits);
    return new Double(curDouble);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeDoubleNullable(Double value) throws IOException, RpcException {
    if (value != null) {
      writeIsNotNull(true);
      long bits = Double.doubleToLongBits(value.doubleValue());
      rpcUtil.writeLongBigEndian(bits);
    } else {
      writeIsNotNull(false);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long readInt64() throws IOException, RpcException {
    return rpcUtil.readLongBigEndian();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeInt64(long vaue) throws IOException {
    rpcUtil.writeLongBigEndian(vaue);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int readInt32() throws IOException {
    return rpcUtil.readIntBigEndian();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeInt32(int value) throws IOException {
    rpcUtil.writeIntBigEndian(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte readInt8() throws IOException {
    return rpcUtil.readByte();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeInt8(byte value) throws IOException {
    rpcUtil.writeByte(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double readDouble() throws IOException, RpcException {
    long bits = rpcUtil.readLongBigEndian();
    return Double.longBitsToDouble(bits);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeDouble(double value) throws IOException, RpcException {
    long bits = Double.doubleToLongBits(value);
    rpcUtil.writeLongBigEndian(bits);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addCompressor(final int type, final Class<? extends DataCompressor> compressor) {
    compressorFactory.addCompressor(type, compressor);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataCompressor getCompressor(final int type) throws RpcException {
    return compressorFactory.getCompressor(type);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void setCoreCodec(CoreCodec coreCodec) {
    Assert.notNull(coreCodec);
    this.rpcUtil = coreCodec;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract void addCodecRepository(final CodecRepository paramCodecRepository);
}