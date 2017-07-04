package com.elminster.easy.rpc.protocol;

import java.io.IOException;

import com.elminster.easy.rpc.exception.RpcException;

/**
 * The protocol interface.
 * @author jinggu
 *
 */
public interface Protocol {

  public void encode() throws IOException, RpcException;
  
  public void decode() throws IOException, RpcException;
}
