package com.elminster.easy.rpc.generator.data;

import java.util.List;

import com.elminster.easy.rpc.idl.IDL;

public interface Call {

  public String getName();
  
  public List<ArgDefinition> getArgs();
  
  public ArgDefinition getArg(int idx);
  
  public ArgDefinition getArg(String name);
  
  public IDL getReturn();
  
  public boolean isVoidCall();
  
  public boolean isNoneArgCall();
}
