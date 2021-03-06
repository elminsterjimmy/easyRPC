package com.elminster.easy.rpc.encoding.impl;

import static com.elminster.easy.rpc.codec.CodecConst.FALSE;
import static com.elminster.easy.rpc.codec.CodecConst.IS_NULL;
import static com.elminster.easy.rpc.codec.CodecConst.NOT_NULL;
import static com.elminster.easy.rpc.codec.CodecConst.TRUE;

import java.io.IOException;
import java.nio.ByteBuffer;
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
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.codec.CodecRepository;
import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.impl.TypeCategory;
import com.elminster.easy.rpc.compressor.DataCompressor;
import com.elminster.easy.rpc.compressor.DataCompressorFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.CodecException;
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

  /** the encoding factory name. */
  private final String name;
  /** class name -> remote type name. */
  protected HashMap<String, String> classNameToRemoteTypeNameMap = new HashMap<>();
  /** remote type name -> class name. */
  protected HashMap<String, String> remoteTypeNameToClassNameMap = new HashMap<>();
  /** class name -> codec class. */
  protected HashMap<String, Class<? extends RpcCodec>> encodingClassMap = new HashMap<>();
  /** class name -> codec instance. */
  protected HashMap<String, RpcCodec> encodingInstanceMap = new HashMap<>();

  private transient Codec coreCodec;
  
  private DataCompressorFactory compressorFactory;
  
  public RpcEncodingFactoryImpl(String encodingName) {
    this.name = encodingName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return this.name;
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
  public RpcCodec getEncodingObject(final Class<?> clazz) throws CodecException {
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
      throw new CodecException("No codec registered for class" + clazz.getName());
    }
    return codec;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcCodec getEncodingObject(final String typeName, final TypeCategory typeCategory) throws CodecException {
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
   * @throws CodecException
   *           on error
   */
  private RpcCodec getEncodingObjectInternal(String typeName, TypeCategory typeCategory) throws CodecException {
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
      throw new CodecException("Could not instantiate Encoder for " + paramClassName);
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
    return NOT_NULL == coreCodec.readByte();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeIsNotNull(final boolean isNotNull) throws IOException {
    if (isNotNull) {
      coreCodec.writeByte(NOT_NULL);
    } else {
      coreCodec.writeByte(IS_NULL);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean readBoolean() throws IOException {
    return TRUE == coreCodec.readByte();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeBoolean(boolean bool) throws IOException {
    if (bool) {
      coreCodec.writeByte(TRUE);
    } else {
      coreCodec.writeByte(FALSE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object readObjectNullable() throws IOException, CodecException {
    Object result = null;
    if (readIsNotNull()) {
      String aliasName = coreCodec.readStringAsciiNullable();
      RpcCodec codec = getEncodingObject(aliasName, TypeCategory.IDL);
      if (null == codec) {
        throw new CodecException("No codec registered for remote type " + aliasName);
      }
      result = codec.decode(this);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeObjectNullable(final Object value) throws IOException, CodecException {
    writeIsNotNull(null != value);
    if (null == value) {
      return;
    }
    RpcCodec codec = null;
    try {
      codec = getEncodingObject(value.getClass());
    } catch (CodecException e) {
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
    coreCodec.writeStringAsciiNullable(remoteTypeName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long readInt64Nullable() throws IOException, CodecException {
    if (!readIsNotNull()) {
      return null;
    }
    return Long.valueOf(coreCodec.readLongBigEndian());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeInt64Nullable(final Long value) throws IOException, CodecException {
    if (null != value) {
      writeIsNotNull(true);
      coreCodec.writeLongBigEndian(value.longValue());
    } else {
      writeIsNotNull(false);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Byte readInt8Nullable() throws IOException, CodecException {
    if (!readIsNotNull()) {
      return null;
    }
    return Byte.valueOf(coreCodec.readByte());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeInt8Nullable(final Byte value) throws IOException, CodecException {
    if (null != value) {
      writeIsNotNull(true);
      coreCodec.writeByte(value.byteValue());
    } else {
      writeIsNotNull(false);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Integer readInt32Nullable() throws IOException, CodecException {
    if (!readIsNotNull()) {
      return null;
    }
    return Integer.valueOf(coreCodec.readIntBigEndian());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeInt32Nullable(final Integer data) throws IOException, CodecException {
    if (data != null) {
      writeIsNotNull(true);
      coreCodec.writeIntBigEndian(data.intValue());
    } else {
      writeIsNotNull(false);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String readStringNullable() throws IOException, CodecException {
    return coreCodec.readStringUTF8Nullable();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeStringNullable(final String value) throws IOException, CodecException {
    coreCodec.writeStringUTF8Nullable(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String readAsciiNullable() throws IOException, CodecException {
    return coreCodec.readStringAsciiNullable();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeAsciiNullable(String value) throws IOException, CodecException {
    coreCodec.writeStringAsciiNullable(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double readDoubleNullable() throws IOException, CodecException {
    if (!readIsNotNull()) {
      return null;
    }
    long bits = coreCodec.readLongBigEndian();
    double curDouble = Double.longBitsToDouble(bits);
    return new Double(curDouble);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeDoubleNullable(Double value) throws IOException, CodecException {
    if (value != null) {
      writeIsNotNull(true);
      long bits = Double.doubleToLongBits(value.doubleValue());
      coreCodec.writeLongBigEndian(bits);
    } else {
      writeIsNotNull(false);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long readInt64() throws IOException, CodecException {
    return coreCodec.readLongBigEndian();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeInt64(long vaue) throws IOException {
    coreCodec.writeLongBigEndian(vaue);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int readInt32() throws IOException {
    return coreCodec.readIntBigEndian();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeInt32(int value) throws IOException {
    coreCodec.writeIntBigEndian(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte readInt8() throws IOException {
    return coreCodec.readByte();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeInt8(byte value) throws IOException {
    coreCodec.writeByte(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double readDouble() throws IOException, CodecException {
    long bits = coreCodec.readLongBigEndian();
    return Double.longBitsToDouble(bits);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeDouble(double value) throws IOException, CodecException {
    long bits = Double.doubleToLongBits(value);
    coreCodec.writeLongBigEndian(bits);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flush() throws IOException {
    coreCodec.flush();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
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
  public void setCodec(Codec coreCodec) {
    Assert.notNull(coreCodec);
    this.coreCodec = coreCodec;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Codec getCodec() {
    return this.coreCodec;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writen(byte[] bytes, int off, int len) throws IOException {
    this.coreCodec.writen(bytes, off, len);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] readn(int off, int len) throws IOException {
    byte[] bytes = new byte[len - off];
    this.coreCodec.readn(bytes, off, len);
    return bytes;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writen(ByteBuffer buffer) throws IOException {
    this.coreCodec.writen(buffer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void readn(ByteBuffer buffer) throws IOException {
    this.coreCodec.readn(buffer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract void addCodecRepository(final CodecRepository paramCodecRepository);
}