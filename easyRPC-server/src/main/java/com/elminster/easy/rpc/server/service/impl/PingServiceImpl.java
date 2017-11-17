package com.elminster.easy.rpc.server.service.impl;

import com.elminster.easy.rpc.server.service.PingService;
import com.elminster.easy.rpc.service.RpcService;

public class PingServiceImpl implements PingService, RpcService {

  @Override
  public boolean ping() {
    return true;
  }

  @Override
  public String[] getServiceMethods() {
    return new String[] {"ping"};
  }

  @Override
  public String getServiceName() {
    return "ping";
  }

  @Override
  public String getServiceVersion() {
    return "1.0.0";
  }
}
