package com.elminster.easy.rpc.protocol.impl;

import java.io.IOException;

import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.AsyncResponseProtocol;

public class AsyncResponseProtocolImpl extends ProtocolImpl implements AsyncResponseProtocol {
  
  private static final byte QUERY_DONE = 10;
  private static final byte DONE = 15;
  private static final byte CANCEL = 20;
  private static final byte GET = 30;
  private String requestId;
  private byte action;
  private long timeout;

  public AsyncResponseProtocolImpl(RpcEncodingFactory encodingFactory) {
    super(encodingFactory);
  }

  @Override
  public void encode() throws IOException, RpcException {
    encodingFactory.writeAsciiNullable(requestId);
    encodingFactory.writeInt8(action);
    encodingFactory.writeInt64(timeout);
  }

  @Override
  public void decode() throws IOException, RpcException {
    this.requestId = encodingFactory.readAsciiNullable();
    this.action = encodingFactory.readInt8();
    this.timeout = encodingFactory.readInt64();
  }

  @Override
  public long getTimeout() {
    return timeout;
  }

  @Override
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  @Override
  public String getRequestId() {
    return requestId;
  }

  @Override
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  @Override
  public void queryIsDone() {
    this.action = QUERY_DONE;
  }

  @Override
  public boolean isQueryDone() {
    return QUERY_DONE == this.action;
  }

  @Override
  public boolean isDone() {
    return DONE == action;
  }

  @Override
  public void setDone(boolean isDone) {
    this.action = DONE;
  }

  @Override
  public void cancel() {
    this.action = CANCEL;
  }

  @Override
  public boolean isCancel() {
    return CANCEL == this.action;
  }

  @Override
  public void setGet() {
    this.action = GET;
  }

  @Override
  public boolean isGet() {
    return GET == this.action;
  }

}
