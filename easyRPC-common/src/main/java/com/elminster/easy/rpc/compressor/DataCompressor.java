package com.elminster.easy.rpc.compressor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.elminster.easy.rpc.exception.RpcException;

public interface DataCompressor {

  public void deflate(Object obj, OutputStream out) throws IOException;
  
  public Object inflate(InputStream in) throws IOException, RpcException;
}
