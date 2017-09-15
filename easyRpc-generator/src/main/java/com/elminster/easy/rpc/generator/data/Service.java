package com.elminster.easy.rpc.generator.data;

public interface Service {

  public void addCall(Call call);
  
  public Call getCall(String callName);
}
