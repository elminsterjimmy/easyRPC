package com.elminster.easy.rpc.generator.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.generator.SourceGenerator;
import com.elminster.easy.rpc.generator.exception.NoSuchVariableDefinitionException;
import com.elminster.easy.rpc.generator.exception.VariableNameAlreadyDefinedException;
import com.elminster.easy.rpc.idl.IDL;
import com.elminster.easy.rpc.idl.impl.BaseIDL;

public class StructImpl extends BaseIDL implements Struct, IDL, SourceGenerator {

  private Map<String, VariableDefinition> members = new HashMap<>();
  
  public StructImpl(String local, String remote, Class<?> typeClass, Class<? extends RpcCodec> codecClass) {
    super(local, remote, typeClass, codecClass);
  }
  
  public Collection<VariableDefinition> getMembers() {
    return Collections.unmodifiableCollection(members.values());
  }
  
  public void addMember(VariableDefinition vd) throws VariableNameAlreadyDefinedException {
    if (null != vd) {
      String vdName = vd.getName();
      if (members.containsKey(vdName)) {
        throw new VariableNameAlreadyDefinedException(String.format("Member with name [%s] is already defined in struct [%s].", vdName, this.getRemoteName()));
      }
      members.put(vdName, vd);
    }
  }
  
  public VariableDefinition getMember(String vdName) throws NoSuchVariableDefinitionException {
    VariableDefinition vd = members.get(vdName);
    if (null == vd) {
      throw new NoSuchVariableDefinitionException(String.format("There is no such memeber with name [%s] defined in strcut [%s].", vdName, this.getRemoteName()));
    }
    return vd;
  }

  @Override
  public String generateSource() {
    
    return null;
  }
}
