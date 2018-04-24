package com.elminster.easy.rpc.data;

import java.util.Arrays;

public class Request {

  private String version;
  private String requestId;
  private Async async;
  private String serviceName;
  private String methodName;
  private Object[] methodArgs;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public Async getAsync() {
    return async;
  }

  public void setAsync(Async async) {
    this.async = async;
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

  public void setMethodArgs(Object[] methodArgs) {
    this.methodArgs = methodArgs;
  }

  @Override
  public String toString() {
    return "Request [version=" + version + ", requestId=" + requestId + ", async=" + async + ", serviceName=" + serviceName + ", methodName=" + methodName + ", methodArgs="
        + Arrays.toString(methodArgs) + "]";
  }
}
