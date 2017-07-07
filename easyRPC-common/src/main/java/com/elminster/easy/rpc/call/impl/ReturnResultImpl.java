package com.elminster.easy.rpc.call.impl;

import com.elminster.easy.rpc.call.ReturnResult;

public class ReturnResultImpl implements ReturnResult {
  
  private final Class<?> returnType;
  private final Object returnValue;
  
  public ReturnResultImpl(Class<?> returnType, Object returnValue) {
    this.returnType = returnType;
    this.returnValue = returnValue;
  }

  @Override
  public Object getReturnValue() {
    return returnValue;
  }

  @Override
  public Class<?> getReturnType() {
    return returnType;
  }

  @Override
  public String toString() {
    return String.format("Return Result [ %s %s ]", returnType, returnValue.toString());
  }
}
