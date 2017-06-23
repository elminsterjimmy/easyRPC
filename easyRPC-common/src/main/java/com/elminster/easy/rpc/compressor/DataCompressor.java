package com.elminster.easy.rpc.compressor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.elminster.easy.rpc.exception.RpcException;

/**
 * The Data Compressor.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface DataCompressor {

  /**
   * Deflate the data.
   * 
   * @param data
   *          the data
   * @param out
   *          the output stream
   * @throws IOException
   *           on error
   */
  public void deflate(final Object data, final OutputStream out) throws IOException;

  /**
   * Inflate the data.
   * 
   * @param in
   *          the input stream
   * @return the data
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on error
   */
  public Object inflate(final InputStream in) throws IOException, RpcException;
}
