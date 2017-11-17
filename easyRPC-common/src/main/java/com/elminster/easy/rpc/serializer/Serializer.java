package com.elminster.easy.rpc.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.elminster.easy.rpc.exception.RpcException;

/**
 * The message serializer.
 * 
 * @author jinggu
 *
 * @param <T>
 *          Serialized Object Type
 * @version 1.0
 */
public interface Serializer<T> {

  /**
   * Serialize the message.
   * 
   * @param message
   *          the message
   * @return byte array
   * @throws IOException
   *           on IO error
   * @throws RpcException
   *           on internal RPC error
   */
  public byte[] serialize(T message) throws IOException, RpcException;

  /**
   * Deserialize the message.
   * 
   * @param message
   *          the message
   * @return deserialized message object
   * @throws IOException
   *           on IO error
   * @throws RpcException
   *           on internal RPC error
   */
  public T deserialize(ByteBuffer message) throws IOException, RpcException;

}