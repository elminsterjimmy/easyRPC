package com.elminster.easy.rpc.request.impl;

import com.elminster.easy.rpc.request.RpcRequest;

/**
 * The RPC Request.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcRequestImpl implements RpcRequest {

  private String requestId;
  private String encoding;
  private boolean isAsyncCall;
  private boolean isVoidCall;
  private String serviceName;
  private String serviceVersion;
  private String methodName;
  private Object[] methodArgs;

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getServiceVersion() {
    return serviceVersion;
  }

  public void setServiceVersion(String version) {
    this.serviceVersion = version;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public boolean isAsyncCall() {
    return isAsyncCall;
  }

  public void setAsyncCall(boolean isAsyncCall) {
    this.isAsyncCall = isAsyncCall;
  }

  public boolean isVoidCall() {
    return isVoidCall;
  }

  public void setVoidCall(boolean isVoidCall) {
    this.isVoidCall = isVoidCall;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public Object[] getMethodArgs() {
    return methodArgs;
  }

  public void setMethodArgs(Object... args) {
    this.methodArgs = args;
  }

  public String toString() {
    return String.format("RPC Request [%s] {%s %s:%s#%s%s}.", 
        requestId, isAsyncCall ? "A" : "S", 
            serviceName, serviceVersion, methodName, generMethodArgs(methodArgs));
  }

  private static String generMethodArgs(Object[] args) {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    boolean first = true;
    if (null != args) {
      for (Object arg : args) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append(arg);
      }
    }
    sb.append(")");
    return sb.toString();
  }
}
