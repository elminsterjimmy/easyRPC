package com.elminster.easy.rpc.protocol;

import com.elminster.easy.rpc.request.AsyncQueryRequest;

public interface AsyncRequestProtocol extends Protocol<AsyncQueryRequest> {
  
  public static final byte QUERY_DONE = 10;
  public static final byte CANCEL = 20;
  public static final byte GET = 30;
}
