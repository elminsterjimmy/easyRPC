package com.elminster.easy.rpc.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import com.elminster.easy.rpc.exception.RpcException;

public interface RpcEncodingFactory {

public String getEncoding();
  
  public void setEncoding(String paramString);
  
  public RpcCodec getEncodingObject(Class<?> paramClass)
    throws RpcException;
  
  public RpcCodec getEncodingObject(String paramString, int paramInt)
    throws RpcException;
  
  public String getClassNameForRemote(String paramString);
  
  public String getRemoteForClassName(String paramString);
  
  public Set<String> getRegisteredClassNames();
  
  public Set<String> getRegisteredRemoteNames();
  
  public boolean readIsNotNull(InputStream paramInputStream)
    throws IOException;
  
  public void writeIsNotNull(OutputStream paramOutputStream, boolean paramBoolean)
    throws IOException;
  
  public Object readObjectNullable(InputStream paramInputStream)
    throws IOException, RpcException;
  
  public Object readObjectNullable(InputStream paramInputStream, Object paramObject)
    throws IOException, RpcException;
  
  public Object readObjectNotNull(InputStream paramInputStream)
    throws IOException, RpcException;
  
  public void writeObjectNullable(OutputStream paramOutputStream, Object paramObject)
    throws IOException, RpcException;
  
  public Object readObjectHeader(InputStream paramInputStream, Class<?> paramClass, Object paramObject)
    throws IOException, RpcException;
  
  public Object readObjectData(InputStream paramInputStream, Class<?> paramClass, Object paramObject)
    throws IOException, RpcException;
  
  public Object readObjectFooter(InputStream paramInputStream, Class<?> paramClass, Object paramObject)
    throws IOException, RpcException;
  
  public void writeObjectNotNull(OutputStream paramOutputStream, Object paramObject)
    throws IOException, RpcException;
  
  public Long readInt64Nullable(InputStream paramInputStream)
    throws IOException, RpcException;
  
  public void writeInt64Nullable(OutputStream paramOutputStream, Long paramLong)
    throws IOException, RpcException;
  
  public Integer readInt32Nullable(InputStream paramInputStream)
    throws IOException, RpcException;
  
  public void writeInt32Nullable(OutputStream paramOutputStream, Integer paramInteger)
    throws IOException, RpcException;
  
  public Byte readInt8Nullable(InputStream paramInputStream)
    throws IOException, RpcException;
  
  public void writeInt8Nullable(OutputStream paramOutputStream, Byte paramByte)
    throws IOException, RpcException;
  
  public String readStringNullable(InputStream paramInputStream)
    throws IOException, RpcException;
  
  public void writeStringNullable(OutputStream paramOutputStream, String paramString)
    throws IOException, RpcException;
  
  public String readStringNotNull(InputStream paramInputStream)
    throws IOException, RpcException;
  
  public void writeStringNotNull(OutputStream paramOutputStream, String paramString)
    throws IOException, RpcException;
  
  public Double readDoubleNullable(InputStream paramInputStream)
    throws IOException, RpcException;
  
  public void writeDoubleNullable(OutputStream paramOutputStream, Double paramDouble)
    throws IOException, RpcException;
  
  public void writeStreamHeader(OutputStream paramOutputStream, Class<?> paramClass, Object paramObject)
    throws IOException, RpcException;
  
  public void writeStreamData(OutputStream paramOutputStream, Class<?> paramClass, Object paramObject)
    throws IOException, RpcException;
  
  public void writeStreamFooter(OutputStream paramOutputStream, Class<?> paramClass, Object paramObject)
    throws IOException, RpcException;
  
  public void addCompressor(int paramInt, Class<? extends DataCompressor> paramClass);
  
  public DataCompressor getCompressor(int paramInt)
    throws RpcException;
  
  public void addCodecRepository(CodecRepository paramCodecRepository);
  
  public void addEncodingClass(Class<?> paramClass1, Class<?> paramClass2, String paramString);
  
  public void addEncodingClass(String paramString1, Class<?> paramClass, String paramString2);
  
  public void addEncodingInstance(Class<?> paramClass, RpcCodec paramRpcCodec, String paramString);
  
  public void addEncodingInstance(String paramString1, RpcCodec paramRpcCodec, String paramString2);
}
