package com.elminster.easy.rpc.idl;

public interface IDLTypes {

  public String getLocalName();
  
  public String getRemoteName();
  
  public Class<?> getTypeClass();
  
  public Class<?> getCodecClass();
}
