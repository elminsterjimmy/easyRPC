package com.elminster.easy.rpc.generator.data;

import org.junit.Test;

import com.elminster.easy.rpc.generator.SourceGenerator;
import com.elminster.easy.rpc.idl.impl.IDLBasicTypes;

public class VariableDefinitionTest {

  @Test
  public void generateSourceTest() {
    VariableDefinition vd = new VariableDefinitionImpl("intA", IDLBasicTypes.INTEGER);
    System.out.println(((SourceGenerator)vd).generateSource());
  }
}
