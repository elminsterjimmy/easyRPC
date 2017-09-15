package com.elminster.easy.rpc.generator.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.elminster.common.util.CollectionUtil;
import com.elminster.easy.rpc.idl.IDL;

public class CallImpl implements Call {
  
  private final String name;
  private final List<ArgDefinition> args;
  private final Map<String, ArgDefinition> argsMap;
  private final IDL rtn;
  
  public CallImpl(String name, List<ArgDefinition> args, IDL rtn) {
    this.name = name;
    this.args = args;
    this.argsMap = new HashMap<>();
    if (null != args) {
      for (ArgDefinition arg : args) {
        argsMap.put(arg.getName(), arg);
      }
    }
    this.rtn = rtn;
  }
  

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<ArgDefinition> getArgs() {
    return Collections.unmodifiableList(args);
  }

  @Override
  public ArgDefinition getArg(int idx) {
    return args.get(idx);
  }

  @Override
  public ArgDefinition getArg(String name) {
    return argsMap.get(name);
  }

  @Override
  public IDL getReturn() {
    return rtn;
  }


  @Override
  public boolean isVoidCall() {
    return Void.class == this.rtn.getTypeClass();
  }


  @Override
  public boolean isNoneArgCall() {
    return CollectionUtil.isEmpty(args);
  }
}
