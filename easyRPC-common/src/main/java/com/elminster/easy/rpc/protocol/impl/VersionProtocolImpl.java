package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.VersionProtocol;

/**
 * The version protocol implementation.
 * 
 * @author jinggu
 * @version 1.0
 */
public class VersionProtocolImpl extends ProtocolImpl implements VersionProtocol {
  
  private String version;

  public VersionProtocolImpl(RpcEncodingFactory encodingFactory) {
    super(encodingFactory);
  }

  @Override
  public void encode() throws IOException, RpcException {
    encodingFactory.writeAsciiNullable(version);
  }

  @Override
  public void decode() throws IOException, RpcException {
    version = encodingFactory.readAsciiNullable();
  }

  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String getVersion() {
    return version;
  }
}
