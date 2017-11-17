package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.VersionProtocol;

/**
 * The version protocol implementation.
 * 
 * @author jinggu
 * @version 1.0
 */
public class VersionProtocolImpl extends ProtocolImpl<String> implements VersionProtocol {
  
  public VersionProtocolImpl() {
  }

  @Override
  public void writeData(String version, RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    encodingFactory.writeAsciiNullable(version);
  }

  @Override
  public String readData(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    return encodingFactory.readAsciiNullable();
  }

}
