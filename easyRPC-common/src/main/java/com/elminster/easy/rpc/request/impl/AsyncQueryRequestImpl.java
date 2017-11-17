package com.elminster.easy.rpc.request.impl;

import com.elminster.easy.rpc.protocol.AsyncRequestProtocol;
import com.elminster.easy.rpc.request.AsyncQueryRequest;

public class AsyncQueryRequestImpl implements AsyncQueryRequest {

  private String requestId;
  private String encoding;
  private String id2Request;
  private long timeout;
  private byte action;
  
  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getId2Request() {
    return id2Request;
  }

  public void setId2Request(String id2Request) {
    this.id2Request = id2Request;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public byte getAction() {
    return action;
  }

  public void setAction(byte action) {
    this.action = action;
  }

  @Override
  public void queryDone() {
    this.action = AsyncRequestProtocol.QUERY_DONE;
  }

  @Override
  public boolean isQueryDone() {
    return AsyncRequestProtocol.QUERY_DONE == this.action;
  }

  @Override
  public void requestGet() {
    this.action = AsyncRequestProtocol.GET;
  }

  @Override
  public boolean isRequestGet() {
    return AsyncRequestProtocol.GET == this.action;
  }

  @Override
  public void cancel() {
    this.action = AsyncRequestProtocol.CANCEL;
  }

  @Override
  public boolean isCancel() {
    return AsyncRequestProtocol.CANCEL == this.action;
  }
}
