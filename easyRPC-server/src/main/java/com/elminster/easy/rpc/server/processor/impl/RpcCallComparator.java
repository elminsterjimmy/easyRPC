package com.elminster.easy.rpc.server.processor.impl;

import java.util.Comparator;

import com.elminster.easy.rpc.call.RpcCall;

public class RpcCallComparator implements Comparator<RpcCall> {

  @Override
  public int compare(RpcCall o1, RpcCall o2) {
    return o1.getPriority() - o2.getPriority();
  }

}
