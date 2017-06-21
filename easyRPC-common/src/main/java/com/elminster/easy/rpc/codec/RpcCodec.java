package com.elminster.easy.rpc.codec;

import java.io.InputStream;
import java.io.OutputStream;

import com.elminster.easy.rpc.exception.RpcException;

public interface RpcCodec {

  public void encode(OutputStream paramOutputStream, Object paramObject, RpcEncodingFactory paramKisRpcEncodingFactory) throws RpcException;

  public Object decode(InputStream paramInputStream, RpcEncodingFactory paramKisRpcEncodingFactory) throws RpcException;

  public Object decode(InputStream paramInputStream, Object paramObject, RpcEncodingFactory paramKisRpcEncodingFactory) throws RpcException;

  public Object convertArray(Object paramObject);
}
