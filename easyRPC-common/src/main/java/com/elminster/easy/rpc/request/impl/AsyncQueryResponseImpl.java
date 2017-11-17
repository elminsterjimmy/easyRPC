package com.elminster.easy.rpc.request.impl;

import com.elminster.easy.rpc.protocol.AsyncResponseProtocol;
import com.elminster.easy.rpc.request.AsyncQueryResponse;

public class AsyncQueryResponseImpl implements AsyncQueryResponse {

  private String requestId;
  private String id2Request;
  private byte action;
  private long timeout;

  @Override
  public long getTimeout() {
    return timeout;
  }

  @Override
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  @Override
  public byte getAction() {
    return action;
  }

  @Override
  public void setAction(byte action) {
    this.action = action;
  }

  @Override
  public String getRequestId() {
    return requestId;
  }

  @Override
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getId2Request() {
    return id2Request;
  }

  public void setId2Request(String id2Request) {
    this.id2Request = id2Request;
  }

  @Override
  public boolean isDone() {
    return AsyncResponseProtocol.DONE == this.action;
  }

  @Override
  public void setDone(boolean isDone) {
    if (isDone) {
      this.action = AsyncResponseProtocol.DONE;
    } else {
      this.action = AsyncResponseProtocol.NOT_DONE;
    }
  }

  @Override
  public boolean isCancelled() {
    return AsyncResponseProtocol.CANCELLED == this.action;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    if (cancelled) {
      this.action = AsyncResponseProtocol.CANCELLED;
    } else {
      this.action = AsyncResponseProtocol.CALCEL_FAILED;
    }
  }
}
