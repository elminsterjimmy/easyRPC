package com.elminster.easy.rpc.it;

import java.sql.Timestamp;

import com.elminster.easy.rpc.service.RpcService;

public class RpcTestServiceImpl implements RpcService, RpcTestIf {

  @Override
  public String[] getServiceMethods() {
    return new String[] {
        "testIntegerPlus",
        "testIntPlus",
        "testLongPlus",
        "testString",
        "now",
        "testVoid",
        "testLongTimeJob"
    };
  }

  @Override
  public String getServiceName() {
    return "Test";
  }

  @Override
  public String getServiceVersion() {
    return "0.1.0";
  }

  @Override
  public String testString(String world) {
    return "hello " + world;
  }

  @Override
  public int testIntPlus(int i) {
    return i + 1;
  }

  @Override
  public Integer testIntegerPlus(Integer i) {
    if (null == i) {
      return 0;
    }
    return i + 1;
  }

  @Override
  public long testLongPlus(long l) {
    return l + 1;
  }

  @Override
  public Timestamp now() {
    return new Timestamp(System.currentTimeMillis());
  }

  @Override
  public String unpublished() {
    return "unpublished";
  }

  @Override
  public void testVoid() {
    System.out.println("void");
  }

  @Override
  public String testLongTimeJob() {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return "Finish Long Time Job";
  }
}
