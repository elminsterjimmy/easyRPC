package com.elminster.easy.rpc.server.listener;

import com.elminster.easy.rpc.context.InvokeContext;
import com.elminster.easy.rpc.server.processor.ReturnResult;

public class RpcProcessEvent {

  private final String serviceName;

  private final String methodName;

  private final Object[] args;
  
  private final ReturnResult result;

  private final InvokeContext context;

  public RpcProcessEvent(String serviceName, String methodName, Object[] args, InvokeContext context) {
    this(serviceName, methodName, args, null, context);
  }
  
  public RpcProcessEvent(String serviceName, String methodName, Object[] args, ReturnResult result, InvokeContext context) {
    this.serviceName = serviceName;
    this.methodName = methodName;
    this.args = args;
    this.result = result;
    this.context = context;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getMethodName() {
    return methodName;
  }

  public Object[] getArgs() {
    return args;
  }

  public ReturnResult getResult() {
    return result;
  }

  public InvokeContext getContext() {
    return context;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Calling ").append(methodName).append(" from ").append(context.toString());
    return sb.toString();
  }
}
