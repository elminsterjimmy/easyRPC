package com.elminster.easy.rpc.codec.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.constants.Constants.StringConstants;
import com.elminster.common.util.StringUtil;
import com.elminster.easy.rpc.codec.CodecController;
import com.elminster.easy.rpc.codec.CodecRepository;
import com.elminster.easy.rpc.codec.CodecRepositoryElement;
import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.compressor.DataCompressor;
import com.elminster.easy.rpc.compressor.DataCompressorFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.idl.IDLType;
import com.elminster.easy.rpc.idl.impl.IDLBasicTypes;
import com.elminster.easy.rpc.util.RpcUtil;

/**
 * The RPC encoding factory implementation.
 * 
 * @author jinggu
 * @version 1.0
 */
public abstract class RpcEncodingFactoryImpl implements RpcEncodingFactory {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(RpcEncodingFactoryImpl.class);

  private static final Pattern REMOTE_ARRAY_PATTERN = Pattern.compile(".*\\[\\]$");
  private static final Pattern LOCAL_ARRAY_PATTERN = Pattern.compile("^\\[.*");
  private static final Pattern REPLACE_ARRAY_PREFIX_PATTERN = Pattern.compile("^\\[*");
  private static final Pattern REPLACE_CLASS_PREFIX_PATTERN = Pattern.compile("^L");
  private static final Pattern REPLACE_CLASS_SUFFIX_PATTERN = Pattern.compile(";");

  protected String encoding = null;
  protected HashMap<String, Class<? extends RpcCodec>> encodingClassMap = new HashMap<>();
  protected HashMap<String, RpcCodec> encodingInstanceMap = new HashMap<>();
  
  /** class name -> remote type name. */
  protected HashMap<String, String> classToRemoteMap = new HashMap<>();
  protected HashMap<String, String> remoteToClassMap = new HashMap<>();
  
  // TODO inject.
  private RpcUtil rpcUtil;
  private DataCompressorFactory compressorFactory;
  private CodecController codecController;

  public String getEncoding() {
    return this.encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void addEncodingClass(Class<?> paramClass, Class<? extends RpcCodec> encClass, String remoteName) {
    addEncodingClass(paramClass.getName(), encClass, remoteName);
  }

  public void addEncodingClass(String paramClassName, Class<? extends RpcCodec> encClass, String remoteName) {
    this.encodingClassMap.put(paramClassName, encClass);
    setRemoteForClassName(paramClassName, remoteName);
  }

  public void addEncodingInstance(Class<?> paramClass, RpcCodec encObject, String remoteName) {
    addEncodingInstance(paramClass.getName(), encObject, remoteName);
  }

  public void addEncodingInstance(String paramClassName, RpcCodec encObject, String remoteName) {
    this.encodingInstanceMap.put(paramClassName, encObject);
    setRemoteForClassName(paramClassName, remoteName);
  }

  public RpcCodec getEncodingObject(Class<?> clazz) throws RpcException {
    if (null == clazz) {
      return null;
    }
    RpcCodec retValue = getEncodingObject(clazz.getName(), TypeCategory.IDL);
    if (retValue == null) {
      Class<?> encParent = getParentClassForEncode(clazz);
      if (encParent != null) {
        retValue = getEncodingObject(encParent);

        addEncodingInstance(clazz, retValue, getRemoteForClassName(encParent.getName()));
      }
    }
    if (retValue == null) {
      throw new RpcException("No codec registered for class" + clazz.getName());
    }
    return retValue;
  }

  public RpcCodec getEncodingObject(String typeName, TypeCategory typeCategory) throws RpcException {
    RpcCodec codec = getEncodingObjectInternal(typeName, typeCategory);
    if (null == codec) {
      String idlName = null;
      String className = null;
      if (TypeCategory.JAVA == typeCategory) {
        idlName = typeName;
      } else {
        className = typeName;
      }
      if (codecController != null) {
        CodecRepositoryElement codecRepoElem = codecController.getCodecRepositoryElement(idlName, className, null);
        if (codecRepoElem != null) {
          addEncodingInstance(codecRepoElem.getClassName(), codecRepoElem.getCodec(), codecRepoElem.getIdlName());
          codec = codecRepoElem.getCodec();
        }
      }
    }
    return codec;
  }

  private RpcCodec getEncodingObjectInternal(String typeName, TypeCategory typeCategory) throws RpcException {
    String paramClassName = null;
    try {
      if (typeName == null) {
        return null;
      }
      RpcCodec curEncObj = null;

      boolean isArray = false;
      switch (typeCategory) {
      case JAVA:
        isArray = typeName.charAt(typeName.length() - 1) == ']';
        break;
      case IDL:
        isArray = typeName.charAt(0) == '[';
        break;
      }
      if (isArray) {
        curEncObj = getDefaultArrayCodec();
        if (TypeCategory.IDL == typeCategory) {
          String cName = getCanonicalName(typeName);
          String remoteTypeName = this.classToRemoteMap.get(cName);
          this.classToRemoteMap.put(typeName, remoteTypeName + "[]");
        }
      }
      if (curEncObj == null) {
        if (TypeCategory.JAVA == typeCategory) {
          paramClassName = this.remoteToClassMap.get(typeName);
          if (paramClassName == null) {
            return null;
          }
        } else {
          paramClassName = typeName;
        }
        curEncObj = this.encodingInstanceMap.get(paramClassName);
        if (curEncObj == null) {
          Class<? extends RpcCodec> encInstanceClass = this.encodingClassMap.get(paramClassName);
          if (encInstanceClass != null) {
            curEncObj = encInstanceClass.newInstance();
          }
        }
      }
      if (curEncObj == null) {
      }
      return getDefaultEncodingObject(paramClassName);
    } catch (Exception e) {
      logger.error(e.toString(), e);
      throw new RpcException("Could not instantiate Encoder for " + paramClassName);
    }
  }

  private String getCanonicalName(String typeName) {
    if (StringUtil.isEmpty(typeName)) {
      return null;
    }
    String result = null;

    String cn = REPLACE_ARRAY_PREFIX_PATTERN.matcher(typeName).replaceAll(StringConstants.EMPTY_STRING);
    cn = REPLACE_CLASS_PREFIX_PATTERN.matcher(cn).replaceFirst(StringConstants.EMPTY_STRING);
    cn = REPLACE_CLASS_SUFFIX_PATTERN.matcher(cn).replaceAll(StringConstants.EMPTY_STRING);

    IDLType bt = IDLBasicTypes.getByName(cn);
    if (bt != null) {
      result = bt.getLocalName();
    } else {
      result = cn;
    }
    return result;
  }

  protected Class<?> getParentClassForEncode(Class<?> paramClass) {
    try {
      Class<?> curParentClass = paramClass.getSuperclass();
      while (curParentClass != null) {
        if ((getEncodingObject(curParentClass) != null) && (!curParentClass.equals(Object.class))) {
          return curParentClass;
        }
        curParentClass = curParentClass.getSuperclass();
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }

  protected RpcCodec getDefaultEncodingObject(String paramClassName) throws RpcException {
    return null;
  }

  protected RpcCodec getDefaultArrayCodec() {
    return null;
  }

  public String getClassNameForRemote(String remoteName) {
    return (String) this.remoteToClassMap.get(remoteName);
  }

  public String getRemoteForClassName(String className) {
    return (String) this.classToRemoteMap.get(className);
  }

  public void setRemoteForClassName(String className, String remoteName) {
    this.classToRemoteMap.put(className, remoteName);
    this.remoteToClassMap.put(remoteName, className);
  }

  public Set<String> getRegisteredClassNames() {
    Set<String> retValue = new HashSet<>();
    retValue.addAll(this.encodingClassMap.keySet());
    retValue.addAll(this.encodingInstanceMap.keySet());
    return retValue;
  }

  public Set<String> getRegisteredRemoteNames() {
    return Collections.unmodifiableSet(remoteToClassMap.keySet());
  }

  public boolean readIsNotNull(InputStream in) throws IOException {
    return rpcUtil.readByte(in) == 1;
  }

  public void writeIsNotNull(OutputStream out, boolean valid) throws IOException {
    if (valid) {
      out.write(1);
    } else {
      out.write(0);
    }
  }

  public Object readObjectNullable(InputStream in) throws IOException, RpcException {
    Object result = null;
    if (readIsNotNull(in)) {
      String aliasName = rpcUtil.readStringAsciiNullable(in);

      RpcCodec codec = getEncodingObject(aliasName, TypeCategory.JAVA);
      if (codec == null) {
        throw new RpcException("No codec registered for remote type " + aliasName);
      }
      result = codec.decode(in, this);
    }
    return result;
  }

  public Object readObjectNotNull(InputStream in) throws IOException, RpcException {
    String aliasName = rpcUtil.readStringAsciiNullable(in);

    RpcCodec codec = getEncodingObject(aliasName, TypeCategory.JAVA);
    if (codec == null) {
      throw new RpcException("No codec registered for remote type " + aliasName);
    }
    return codec.decode(in, this);
  }

  public void writeObjectNullable(OutputStream out, Object data) throws IOException, RpcException {
    writeIsNotNull(out, data != null);
    if (data == null) {
      return;
    }
    RpcCodec codec = null;
    try {
      codec = getEncodingObject(data.getClass());
    } catch (RpcException e) {
      if ((data instanceof Throwable)) {
        logger.error("No codec registered: " + data.getClass() + " call stack ", (Throwable) data);
      }
      throw e;
    }
    encodeClassName(out, data.getClass());

    codec.encode(out, data, this);
  }

  public void writeObjectNotNull(OutputStream out, Object data) throws IOException, RpcException {
    RpcCodec codec = getEncodingObject(data.getClass());

    encodeClassName(out, data.getClass());

    codec.encode(out, data, this);
  }

  private void encodeClassName(OutputStream out, Class<?> theClass) throws IOException {
    String className = theClass.getName();
    String remoteTypeName = getRemoteForClassName(className);
    rpcUtil.writeStringAsciiNullable(out, remoteTypeName);
  }

  public Long readInt64Nullable(InputStream in) throws IOException, RpcException {
    if (!readIsNotNull(in)) {
      return null;
    }
    return Long.valueOf(rpcUtil.readLongBigEndian(in));
  }

  public void writeInt64Nullable(OutputStream out, Long data) throws IOException, RpcException {
    if (data != null) {
      writeIsNotNull(out, true);
      rpcUtil.writeLongBigEndian(out, data.longValue());
    } else {
      writeIsNotNull(out, false);
    }
  }

  public Byte readInt8Nullable(InputStream in) throws IOException, RpcException {
    if (!readIsNotNull(in)) {
      return null;
    }
    return Byte.valueOf(rpcUtil.readByte(in));
  }

  public void writeInt8Nullable(OutputStream out, Byte data) throws IOException, RpcException {
    if (data != null) {
      writeIsNotNull(out, true);
      rpcUtil.writeByte(out, data.byteValue());
    } else {
      writeIsNotNull(out, false);
    }
  }

  public Integer readInt32Nullable(InputStream in) throws IOException, RpcException {
    if (!readIsNotNull(in)) {
      return null;
    }
    return Integer.valueOf(rpcUtil.readIntBigEndian(in));
  }

  public void writeInt32Nullable(OutputStream out, Integer data) throws IOException, RpcException {
    if (data != null) {
      writeIsNotNull(out, true);
      rpcUtil.writeIntBigEndian(out, data.intValue());
    } else {
      writeIsNotNull(out, false);
    }
  }

  public String readStringNullable(InputStream in) throws IOException, RpcException {
    return rpcUtil.readStringUTF8Nullable(in);
  }

  public void writeStringNullable(OutputStream out, String data) throws IOException, RpcException {
    rpcUtil.writeStringUTF8Nullable(out, data);
  }

  public String readStringNotNull(InputStream in) throws IOException, RpcException {
    return rpcUtil.readStringUTF8(in);
  }

  public void writeStringNotNull(OutputStream out, String data) throws IOException, RpcException {
    rpcUtil.writeStringUTF8(out, data);
  }

  public Double readDoubleNullable(InputStream in) throws IOException, RpcException {
    if (!readIsNotNull(in)) {
      return null;
    }
    long bits = rpcUtil.readLongBigEndian(in);
    double curDouble = Double.longBitsToDouble(bits);
    return new Double(curDouble);
  }

  public void writeDoubleNullable(OutputStream out, Double data) throws IOException, RpcException {
    if (data != null) {
      writeIsNotNull(out, true);

      long bits = Double.doubleToLongBits(data.doubleValue());

      rpcUtil.writeLongBigEndian(out, bits);
    } else {
      writeIsNotNull(out, false);
    }
  }

  public void addCompressor(int type, Class<? extends DataCompressor> compressor) {
    compressorFactory.addCompressor(type, compressor);
  }

  public DataCompressor getCompressor(int type) throws RpcException {
    return compressorFactory.getCompressor(type);
  }

  public abstract void addCodecRepository(CodecRepository paramCodecRepository);
}