package com.elminster.easy.rpc.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import com.elminster.easy.rpc.codec.impl.TypeCategory;
import com.elminster.easy.rpc.compressor.DataCompressor;
import com.elminster.easy.rpc.exception.RpcException;

public interface RpcEncodingFactory {

  public String getEncoding();

  public void setEncoding(String encoding);

  public RpcCodec getEncodingObject(Class<?> clazz) throws RpcException;

  public RpcCodec getEncodingObject(String typeName, TypeCategory typeCategory) throws RpcException;

  public String getClassNameForRemote(String remoteTypeName);

  public String getRemoteForClassName(String classname);

  public Set<String> getRegisteredClassNames();

  public Set<String> getRegisteredRemoteNames();

  public boolean readIsNotNull(InputStream in) throws IOException;

  public void writeIsNotNull(OutputStream out, boolean notNull) throws IOException;

  public Object readObjectNullable(InputStream in) throws IOException, RpcException;

  public void writeObjectNullable(OutputStream out, Object value) throws IOException, RpcException;

  public Long readInt64Nullable(InputStream in) throws IOException, RpcException;

  public void writeInt64Nullable(OutputStream out, Long longValue) throws IOException, RpcException;

  public Integer readInt32Nullable(InputStream in) throws IOException, RpcException;

  public void writeInt32Nullable(OutputStream out, Integer int32Value) throws IOException, RpcException;

  public Byte readInt8Nullable(InputStream in) throws IOException, RpcException;

  public void writeInt8Nullable(OutputStream out, Byte byteValue) throws IOException, RpcException;

  public String readStringNullable(InputStream in) throws IOException, RpcException;

  public void writeStringNullable(OutputStream out, String stringValue) throws IOException, RpcException;

  public Double readDoubleNullable(InputStream in) throws IOException, RpcException;

  public void writeDoubleNullable(OutputStream out, Double doubleValue) throws IOException, RpcException;

  public void addCompressor(int type, Class<? extends DataCompressor> dataCompressor);

  public DataCompressor getCompressor(int type) throws RpcException;

  public void addCodecRepository(CodecRepository paramCodecRepository);

  public void addEncodingClass(Class<?> paramClass, Class<? extends RpcCodec> encClass, String remoteName);

  public void addEncodingClass(String paramClassName, Class<? extends RpcCodec> encClass, String remoteName);

  public void addEncodingInstance(Class<?> paramClass, RpcCodec encObject, String remoteName);

  public void addEncodingInstance(String paramClassName, RpcCodec encObject, String remoteName);
  
  
  
  
}
