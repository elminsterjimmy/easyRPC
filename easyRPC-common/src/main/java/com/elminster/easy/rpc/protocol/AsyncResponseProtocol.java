package com.elminster.easy.rpc.protocol;

import com.elminster.easy.rpc.request.AsyncQueryResponse;

public interface AsyncResponseProtocol extends Protocol<AsyncQueryResponse> {
  
  public static final byte DONE = 15;
  public static final byte NOT_DONE = 16;
  public static final byte CANCELLED = 21;
  public static final byte CALCEL_FAILED = 22;
}
