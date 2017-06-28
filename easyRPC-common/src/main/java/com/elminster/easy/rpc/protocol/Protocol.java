package com.elminster.easy.rpc.protocol;

import java.io.IOException;

public interface Protocal {

  public void encode() throws IOException;
  
  public void decode() throws IOException;
  
  public void ready() throws IOException;
}
