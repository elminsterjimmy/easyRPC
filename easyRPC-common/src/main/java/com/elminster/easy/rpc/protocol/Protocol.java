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
  
  public void complete() throws IOException;
  
  public boolean isCompleted() throws IOException;
  
  public void fail() throws IOException;
}
