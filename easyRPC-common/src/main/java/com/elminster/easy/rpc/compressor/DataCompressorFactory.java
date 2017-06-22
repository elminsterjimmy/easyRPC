package com.elminster.easy.rpc.compressor;

import com.elminster.easy.rpc.exception.RpcException;

public interface DataCompressorFactory {

  public DataCompressor getCompressor(int param) throws RpcException;
  
  public void addCompressor(int param, Class<? extends DataCompressor> compressorClass);
  
  public int version();
}
