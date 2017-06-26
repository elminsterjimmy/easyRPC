package com.elminster.easy.rpc.registery;

public interface Registrable<T> {

  public void register(T t);
  
  public void unregister(T t);
}
