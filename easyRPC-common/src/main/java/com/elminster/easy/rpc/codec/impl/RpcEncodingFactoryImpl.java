package com.elminster.easy.rpc.codec.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.elminster.common.constants.Constants.StringConstants;
import com.elminster.common.util.StringUtil;
import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.idl.IDLTypes;
import com.elminster.easy.rpc.idl.impl.IDLBasicTypes;

public abstract class RpcEncodingFactoryImpl implements RpcEncodingFactory {

  private static Logger log = LoggerFactory.getLogger(KisRpcEncodingFactoryImpl.class);

  private static final Pattern REMOTE_ARRAY_PATTERN = Pattern.compile(".*\\[\\]$");
  private static final Pattern LOCAL_ARRAY_PATTERN = Pattern.compile("^\\[.*");
  private static final Pattern REPLACE_ARRAY_PREFIX_PATTERN = Pattern.compile("^\\[*");
  private static final Pattern REPLACE_CLASS_PREFIX_PATTERN = Pattern.compile("^L");
  private static final Pattern REPLACE_CLASS_SUFFIX_PATTERN = Pattern.compile(";");

  protected String encoding = null;
  protected HashMap<String, Class<?>> encodingClassMap = new HashMap<>();
  protected HashMap<String, RpcCodec> encodingInstanceMap = new HashMap<>();
  protected HashMap<String, String> classToRemoteMap = new HashMap<>();
  protected HashMap<String, String> remoteToClassMap = new HashMap<>();

  public String getEncoding() {
    return this.encoding;
  }

  public void setEncoding(String theEncoding) {
    this.encoding = theEncoding;
  }

  public void addEncodingClass(Class<?> paramClass, Class<?> encClass, String remoteName) {
    this.encodingClassMap.put(paramClass.getName(), encClass);
    setRemoteForClassName(paramClass.getName(), remoteName);
  }

  public void addEncodingClass(String paramClassName, Class<?> encClass, String remoteName) {
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

  public RpcCodec getEncodingObject(Class<?> paramClass) throws RpcException {
    if (null == paramClass) {
      return null;
    }
    RpcCodec retValue = getEncodingObject(paramClass.getName(), 1);
    if (retValue == null) {
      Class<?> encParent = getParentClassForEncode(paramClass);
      if (encParent != null) {
        retValue = getEncodingObject(encParent);

        addEncodingInstance(paramClass, retValue, getRemoteForClassName(encParent.getName()));
      }
    }
    if (retValue == null) {
      throw new RpcException("No codec registered for class" + paramClass.getName());
    }
    return retValue;
  }

  public RpcCodec getEncodingObject(String typeName, TypeCategory typeCategory) throws RpcException {
    RpcCodec codec = getEncodingObjectInternal(typeName, typeCategory);
    if (null == codec) {
      String idlName = null;
      String className = null;
      if (typeCategory == 0) {
        idlName = typeName;
      } else {
        className = typeName;
      }
      CodecController ctrl = CoreServiceRegistry.INSTANCE.getCodecController();
      if (ctrl != null) {
        CodecRepositoryElement codecRepoElem = ctrl.getCodecRepositoryElement(idlName, className, null);
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
          String remoteTypeName = (String) this.classToRemoteMap.get(cName);
          this.classToRemoteMap.put(typeName, remoteTypeName + "[]");
        }
      }
      if (curEncObj == null) {
        if (TypeCategory.JAVA == typeCategory) {
          paramClassName = (String) this.remoteToClassMap.get(typeName);
          if (paramClassName == null) {
            return null;
          }
        } else {
          paramClassName = typeName;
        }
        curEncObj = (RpcCodec) this.encodingInstanceMap.get(paramClassName);
        if (curEncObj == null) {
          Class<?> encInstanceClass = (Class<?>) this.encodingClassMap.get(paramClassName);
          if (encInstanceClass != null) {
            curEncObj = (RpcCodec) encInstanceClass.newInstance();
          }
        }
      }
      if (curEncObj == null) {
      }
      return getDefaultEncodingObject(paramClassName);
    } catch (Exception e) {
      log.error(e.toString(), e);
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

    IDLTypes bt = IDLBasicTypes.getByName(cn);
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

  public boolean readIsNotNull(InputStream iStream) throws IOException {
    return CoreServiceRegistry.INSTANCE.getKisRpcUtil().readByte(iStream) == 1;
  }

  public void writeIsNotNull(OutputStream oStream, boolean valid) throws IOException {
    if (valid) {
      oStream.write(1);
    } else {
      oStream.write(0);
    }
  }

  public Object readObjectNullable(InputStream iStream) throws IOException, RpcException {
    return readObjectNullable(iStream, null);
  }

  public Object readObjectNullable(InputStream iStream, Object codecData) throws IOException, RpcException {
    Object result = null;
    if (readIsNotNull(iStream)) {
      String aliasName = CoreServiceRegistry.INSTANCE.getKisRpcUtil().readStringAsciiNullable(iStream);

      RpcCodec codec = getEncodingObject(aliasName, 0);
      if (codec == null) {
        throw new RpcException("No codec registered for remote type " + aliasName);
      }
      if (codecData != null) {
        result = codec.decode(iStream, codecData, this);
      } else {
        result = codec.decode(iStream, this);
      }
    }
    return result;
  }

  public Object readObjectNotNull(InputStream iStream) throws IOException, RpcException {
    String aliasName = CoreServiceRegistry.INSTANCE.getKisRpcUtil().readStringAsciiNullable(iStream);

    RpcCodec codec = getEncodingObject(aliasName, 0);
    if (codec == null) {
      throw new RpcException("No codec registered for remote type " + aliasName);
    }
    return codec.decode(iStream, this);
  }

  public void writeObjectNullable(OutputStream oStream, Object data) throws IOException, RpcException {
    writeIsNotNull(oStream, data != null);
    if (data == null) {
      return;
    }
    RpcCodec codec = null;
    try {
      codec = getEncodingObject(data.getClass());
    } catch (RpcException e) {
      if ((data instanceof Throwable)) {
        log.error("No codec registered: " + data.getClass() + " call stack ", (Throwable) data);
      }
      throw e;
    }
    encodeClassName(oStream, data.getClass());

    codec.encode(oStream, data, this);
  }

  public Object readObjectHeader(InputStream iStream, Class<?> streamClass, Object codecData) throws IOException, RpcException {
    Object result = null;
    if (readIsNotNull(iStream)) {
      String aliasName = CoreServiceRegistry.INSTANCE.getKisRpcUtil().readStringAsciiNullable(iStream);

      RpcCodec c = getEncodingObject(aliasName, 0);
      if (c == null) {
        throw new RpcException("No codec registered for remote type " + aliasName);
      }
      KisRpcStreamCodec codec = getStreamCodec(streamClass);
      if (codec != c) {
        throw new RpcException("Expected class " + codec.toString() + ", received remote type " + aliasName);
      }
      result = codec.decodeStreamHeader(iStream, codecData, this);
    }
    return result;
  }

  public Object readObjectData(InputStream iStream, Class<?> streamClass, Object codecData) throws IOException, RpcException {
    KisRpcStreamCodec codec = getStreamCodec(streamClass);
    Object result = codec.decodeStreamData(iStream, codecData, this);
    return result;
  }

  public Object readObjectFooter(InputStream iStream, Class<?> streamClass, Object codecData) throws IOException, RpcException {
    KisRpcStreamCodec codec = getStreamCodec(streamClass);
    Object result = codec.decodeStreamFooter(iStream, codecData, this);
    return result;
  }

  public void writeObjectNotNull(OutputStream oStream, Object data) throws IOException, RpcException {
    RpcCodec codec = getEncodingObject(data.getClass());

    encodeClassName(oStream, data.getClass());

    codec.encode(oStream, data, this);
  }

  public void writeStreamHeader(OutputStream oStream, Class<?> streamClass, Object header) throws IOException, RpcException {
    if (header != null) {
      writeIsNotNull(oStream, true);
      KisRpcStreamCodec codec = getStreamCodec(streamClass);
      encodeClassName(oStream, streamClass);
      codec.encodeStreamHeader(oStream, header, this);
    } else {
      writeIsNotNull(oStream, false);
    }
  }

  public void writeStreamData(OutputStream oStream, Class<?> streamClass, Object data) throws IOException, RpcException {
    KisRpcStreamCodec codec = getStreamCodec(streamClass);
    codec.encodeStreamData(oStream, data, this);
  }

  public void writeStreamFooter(OutputStream oStream, Class<?> streamClass, Object footer) throws IOException, RpcException {
    KisRpcStreamCodec codec = getStreamCodec(streamClass);
    codec.encodeStreamFooter(oStream, footer, this);
  }

  private void encodeClassName(OutputStream oStream, Class<?> theClass) throws IOException {
    String className = theClass.getName();
    String remoteTypeName = getRemoteForClassName(className);
    CoreServiceRegistry.INSTANCE.getKisRpcUtil().writeStringAsciiNullable(oStream, remoteTypeName);
  }

  private KisRpcStreamCodec getStreamCodec(Class<?> streamClass) throws RpcException {
    RpcCodec codec = getEncodingObject(streamClass);
    if (codec == null) {
      throw new RpcException("No codec registered for class" + streamClass.getName());
    }
    if ((codec instanceof KisRpcStreamCodec)) {
      return (KisRpcStreamCodec) codec;
    }
    if ((codec instanceof RpcCodecAdapter)) {
      return (KisRpcStreamCodec) ((RpcCodecAdapter) codec).getWrappedCodec();
    }
    throw new RpcException("No stream codec registered for class" + streamClass.getName());
  }

  public Long readInt64Nullable(InputStream iStream) throws IOException, RpcException {
    if (!readIsNotNull(iStream)) {
      return null;
    }
    return Long.valueOf(CoreServiceRegistry.INSTANCE.getKisRpcUtil().readLongBigEndian(iStream));
  }

  public void writeInt64Nullable(OutputStream oStream, Long data) throws IOException, RpcException {
    if (data != null) {
      writeIsNotNull(oStream, true);
      CoreServiceRegistry.INSTANCE.getKisRpcUtil().writeLongBigEndian(oStream, data.longValue());
    } else {
      writeIsNotNull(oStream, false);
    }
  }

  public Byte readInt8Nullable(InputStream iStream) throws IOException, RpcException {
    if (!readIsNotNull(iStream)) {
      return null;
    }
    return Byte.valueOf(CoreServiceRegistry.INSTANCE.getKisRpcUtil().readByte(iStream));
  }

  public void writeInt8Nullable(OutputStream oStream, Byte data) throws IOException, RpcException {
    if (data != null) {
      writeIsNotNull(oStream, true);
      CoreServiceRegistry.INSTANCE.getKisRpcUtil().writeByte(oStream, data.byteValue());
    } else {
      writeIsNotNull(oStream, false);
    }
  }

  public Integer readInt32Nullable(InputStream iStream) throws IOException, RpcException {
    if (!readIsNotNull(iStream)) {
      return null;
    }
    return Integer.valueOf(CoreServiceRegistry.INSTANCE.getKisRpcUtil().readIntBigEndian(iStream));
  }

  public void writeInt32Nullable(OutputStream oStream, Integer data) throws IOException, RpcException {
    if (data != null) {
      writeIsNotNull(oStream, true);
      CoreServiceRegistry.INSTANCE.getKisRpcUtil().writeIntBigEndian(oStream, data.intValue());
    } else {
      writeIsNotNull(oStream, false);
    }
  }

  public String readStringNullable(InputStream iStream) throws IOException, RpcException {
    return CoreServiceRegistry.INSTANCE.getKisRpcUtil().readStringUTF8Nullable(iStream);
  }

  public void writeStringNullable(OutputStream oStream, String data) throws IOException, RpcException {
    CoreServiceRegistry.INSTANCE.getKisRpcUtil().writeStringUTF8Nullable(oStream, data);
  }

  public String readStringNotNull(InputStream iStream) throws IOException, RpcException {
    return CoreServiceRegistry.INSTANCE.getKisRpcUtil().readStringUTF8(iStream);
  }

  public void writeStringNotNull(OutputStream oStream, String data) throws IOException, RpcException {
    CoreServiceRegistry.INSTANCE.getKisRpcUtil().writeStringUTF8(oStream, data);
  }

  public Double readDoubleNullable(InputStream iStream) throws IOException, RpcException {
    if (!readIsNotNull(iStream)) {
      return null;
    }
    long bits = CoreServiceRegistry.INSTANCE.getKisRpcUtil().readLongBigEndian(iStream);
    double curDouble = Double.longBitsToDouble(bits);
    return new Double(curDouble);
  }

  public void writeDoubleNullable(OutputStream oStream, Double data) throws IOException, RpcException {
    if (data != null) {
      writeIsNotNull(oStream, true);

      long bits = Double.doubleToLongBits(data.doubleValue());

      CoreServiceRegistry.INSTANCE.getKisRpcUtil().writeLongBigEndian(oStream, bits);
    } else {
      writeIsNotNull(oStream, false);
    }
  }

  public void addCompressor(int type, Class<? extends DataCompressor> compressor) {
    CoreServiceRegistry.INSTANCE.getStreamingFactory().addCompressor(type, compressor);
  }

  public DataCompressor getCompressor(int type) throws RpcException {
    return CoreServiceRegistry.INSTANCE.getStreamingFactory().getCompressor(type);
  }

  public abstract void addCodecRepository(CodecRepository paramCodecRepository);
}