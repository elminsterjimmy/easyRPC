package com.elminster.easy.rpc.generator.data;

import java.util.HashMap;
import java.util.Map;

import com.elminster.easy.rpc.generator.SimpleTemplateEngine;
import com.elminster.easy.rpc.generator.SourceGenerator;
import com.elminster.easy.rpc.idl.IDL;

public class VariableDefinitionImpl implements VariableDefinition, SourceGenerator {

  private static final String NAME = "name";
  private static final String TYPE = "type";
  private final String name;
  private final IDL idl;
  
  public VariableDefinitionImpl(String name, IDL idl) {
    this.name = name;
    this.idl = idl;
  }

  @Override
  public String generateSource() {
    Map<String, String> map = new HashMap<>();
    map.put(NAME, name);
    map.put(TYPE, idl.getLocalName());
    return SimpleTemplateEngine.renderTemplate(SimpleTemplateEngine.VARIABLE_DEFINITION_TEMPLATE, map);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public IDL getIDL() {
    return idl;
  }
}
