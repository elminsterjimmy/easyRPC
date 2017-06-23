package com.elminster.easy.rpc.compressor;

import com.elminster.easy.rpc.exception.RpcException;

/**
 * The Data Compressor Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface DataCompressorFactory {

  /**
   * Get the Data Compressor by type.
   * 
   * @param type
   *          the Data Compressor type
   * @return the Data Compressor
   * @throws RpcException
   *           on error
   */
  public DataCompressor getCompressor(final int type) throws RpcException;

  /**
   * Add a data compressor.
   * 
   * @param type
   *          the data compressor type
   * @param compressorClass
   *          the data compressor class
   */
  public void addCompressor(int type, Class<? extends DataCompressor> compressorClass);

  /**
   * Get the factory version.
   * 
   * @return the factory versions
   */
  public int version();
}