package com.elminster.easy.rpc.protocol;

import java.io.IOException;

import com.elminster.easy.rpc.exception.CodecException;

/**
 * A data frame.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface Frame<T> {

  /**
   * Encoding the frame.
   * 
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public void encode(T data) throws IOException, CodecException;

  /**
   * Decoding the frame.
   * 
   * @throws IOException
   *           on error
   * @throws CodecException
   *           on error
   */
  public T decode() throws IOException, CodecException;
}
