package com.elminster.easy.rpc.generator.data;

import com.elminster.easy.rpc.generator.exception.NoSuchVariableDefinitionException;
import com.elminster.easy.rpc.generator.exception.VariableNameAlreadyDefinedException;
import com.elminster.easy.rpc.idl.IDL;

public interface Struct extends IDL {

  public void addMember(VariableDefinition vd) throws VariableNameAlreadyDefinedException;
  
  public VariableDefinition getMember(String vdName) throws NoSuchVariableDefinitionException;
}
