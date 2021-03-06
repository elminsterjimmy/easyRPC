package com.elminster.easy.rpc.encoding;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;

import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.codec.CodecRepository;
import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.impl.TypeCategory;
import com.elminster.easy.rpc.compressor.DataCompressor;
import com.elminster.easy.rpc.exception.CodecException;
import com.elminster.easy.rpc.exception.RpcException;

/**
 * The RPC Encoding Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcEncodingFactory extends Cloneable {

  /**
   * Get the encoding name.
   * 
   * @return the encoding name
   */
  public String getName();

  /**
   * Get the Codec for specified class.
   * 
   * @param clazz
   *          the class
   * @return the codec corresponded to the class
   * @throws CodecException
   *           on error
   */
  public RpcCodec getEncodingObject(final Class<?> clazz) throws CodecException;

  /**
   * Get the Codec for specified class.
   * 
   * @param typeName
   *          the type name
   * @param typeCategory
   *          the type category
   * @return the codec corresponded to the class
   * @throws CodecException
   *           on error
   */
  public RpcCodec getEncodingObject(final String typeName, final TypeCategory typeCategory) throws CodecException;

  /**
   * Get the class name for remote type name.
   * 
   * @param remoteTypeName
   *          the remote type name
   * @return the class name
   */
  public String getClassNameForRemoteName(final String remoteTypeName);

  /**
   * Get the remote type name from the class name.
   * 
   * @param classname
   *          the class name
   * @return the remote type name
   */
  public String getRemoteNameForClassName(final String classname);

  /**
   * Get the all registered class names.
   * 
   * @return the all registered class names
   */
  public Set<String> getRegisteredClassNames();

  /**
   * Get the all registered remote type names.
   * 
   * @return the all registered remote type names
   */
  public Set<String> getRegisteredRemoteNames();

  /**
   * Read next object is not null from input stream.
   * 
   * @return if the next object is not
   * @throws IOException
   *           on error
   */
  public boolean readIsNotNull() throws IOException;

  /**
   * Write next object is not null or not.
   * 
   * @param isNotNull
   *          is not null?
   * @throws IOException
   *           on error
   */
  public void writeIsNotNull(final boolean isNotNull) throws IOException;

  /**
   * Read a boolean.
   * 
   * @return a boolean
   * @throws IOException
   *           on error
   */
  public boolean readBoolean() throws IOException;

  /**
   * Write a boolean.
   * 
   * @param bool
   *          the boolean
   * @throws IOException
   *           on error
   */
  public void writeBoolean(final boolean bool) throws IOException;

  /**
   * Read a nullable object.
   * 
   * @return a nullable object
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public Object readObjectNullable() throws IOException, CodecException;

  /**
   * Write a nullable object.
   * 
   * @param value
   *          the value
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public void writeObjectNullable(final Object value) throws IOException, CodecException;

  /**
   * Read a nullable int64.
   * 
   * @return a nullable int64
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public Long readInt64Nullable() throws IOException, CodecException;

  /**
   * Read an int64.
   * 
   * @return an int64
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public long readInt64() throws IOException, CodecException;

  /**
   * Write a nullable int64.
   * 
   * @param value
   *          the value
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public void writeInt64Nullable(final Long vaue) throws IOException, CodecException;

  /**
   * Write an int64.
   * 
   * @param value
   *          the value
   * @throws IOException
   *           on error
   */
  public void writeInt64(final long vaue) throws IOException;

  /**
   * Read a nullable int32.
   * 
   * @return a nullable int32
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public Integer readInt32Nullable() throws IOException, CodecException;

  /**
   * Read an int32.
   * 
   * @return an int32
   * @throws IOException
   *           on error
   */
  public int readInt32() throws IOException;

  /**
   * Write a nullable int32.
   * 
   * @param value
   *          the value
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public void writeInt32Nullable(final Integer value) throws IOException, CodecException;

  /**
   * Write an int32.
   * 
   * @param value
   *          the value
   * @throws IOException
   *           on error
   */
  public void writeInt32(final int value) throws IOException;

  /**
   * Read a nullable int8.
   * 
   * @return a nullable int8
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public Byte readInt8Nullable() throws IOException, CodecException;

  /**
   * Read an int8.
   * 
   * @return an int8
   * @throws IOException
   *           on error
   */
  public byte readInt8() throws IOException;

  /**
   * Write a nullable int8.
   * 
   * @param value
   *          the value
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public void writeInt8Nullable(final Byte value) throws IOException, CodecException;

  /**
   * Write an int8.
   * 
   * @param value
   *          the value
   * @throws IOException
   *           on error
   */
  public void writeInt8(final byte value) throws IOException;

  /**
   * Read a nullable String.
   * 
   * @return a nullable String
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public String readStringNullable() throws IOException, CodecException;

  /**
   * Write a nullable String.
   * 
   * @param value
   *          the value
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public void writeStringNullable(final String value) throws IOException, CodecException;

  /**
   * Read a nullable ASCII.
   * 
   * @return a nullable int64
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public String readAsciiNullable() throws IOException, CodecException;

  /**
   * Write a nullable ASCII.
   * 
   * @param value
   *          the value
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public void writeAsciiNullable(final String value) throws IOException, CodecException;

  /**
   * Read a nullable double.
   * 
   * @return a nullable double
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public Double readDoubleNullable() throws IOException, CodecException;

  /**
   * Read a double.
   * 
   * @return a double
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public double readDouble() throws IOException, CodecException;

  /**
   * Write a nullable double.
   * 
   * @param value
   *          the value
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public void writeDoubleNullable(final Double value) throws IOException, CodecException;

  /**
   * Write a double.
   * 
   * @param value
   *          the value
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public void writeDouble(final double value) throws IOException, CodecException;
  
  public void writen(final byte[] bytes, int off, int len) throws IOException;
  
  public void writen(final ByteBuffer buffer) throws IOException;
  
  public byte[] readn(int off, int len) throws IOException;
  
  public void readn(final ByteBuffer buffer) throws IOException;
  
  /**
   * Flush to underlayer stream.
   * 
   * @throws IOException
   *           on error
   */
  public void flush() throws IOException;

  /**
   * Add a data compressor.
   * 
   * @param type
   *          the compressor type
   * @param dataCompressor
   *          the data compressor
   */
  public void addCompressor(final int type, final Class<? extends DataCompressor> dataCompressor);

  /**
   * Get the data compressor by the type.
   * 
   * @param type
   *          the type
   * @return the data compressor
   * @throws CodecException
   *           on error
   */
  public DataCompressor getCompressor(int type) throws RpcException;

  /**
   * Add a codec repository.
   * 
   * @param codecRepository
   *          the codec repository
   */
  public void addCodecRepository(final CodecRepository codecRepository);

  /**
   * Add an encoding class for specified class and remote type name.
   * 
   * @param clazz
   *          the class
   * @param encClass
   *          the encoding class
   * @param remoteName
   *          the remote type name
   */
  public void addEncodingClass(final Class<?> clazz, final Class<? extends RpcCodec> encClass, final String remoteName);

  /**
   * Add an encoding class for specified class and remote type name.
   * 
   * @param className
   *          the class name
   * @param encClass
   *          the encoding class
   * @param remoteName
   *          the remote type name
   */
  public void addEncodingClass(final String className, final Class<? extends RpcCodec> encClass, final String remoteName);

  /**
   * Add an encoding instance for specified class and remote type name.
   * 
   * @param clazz
   *          the class
   * @param encObject
   *          the encoding instance
   * @param remoteName
   *          the remote type name
   */
  public void addEncodingInstance(final Class<?> clazz, final RpcCodec encObject, final String remoteName);

  /**
   * Add an encoding instance for specified class and remote type name.
   * 
   * @param className
   *          the class name
   * @param encObject
   *          the encoding instance
   * @param remoteName
   *          the remote type name
   */
  public void addEncodingInstance(final String className, final RpcCodec encObject, final String remoteName);

  /**
   * Inject the core codec.
   * 
   * @param coreCodec
   *          the core codec
   */
  public void setCodec(Codec coreCodec);
  
  /**
   * Get the core codec.
   * @return the core codec
   */
  public Codec getCodec();

  /**
   * Clone the encoding factory.
   * 
   * @return cloned encoding factory
   */
  public RpcEncodingFactory cloneEncodingFactory();
}
