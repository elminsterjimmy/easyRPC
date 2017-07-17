package com.elminster.easy.rpc.it;

import java.sql.Timestamp;

import com.elminster.easy.rpc.idl.Async;
import com.elminster.easy.rpc.service.Rpc;

@Rpc("Test")
public interface RpcTestIf {

  public String testString(String world);
  
  public int testIntPlus(int i);
  
  public Integer testIntegerPlus(Integer i);
  
  public long testLongPlus(long l);

  public Timestamp now();
  
  public String unpublished();
  
  public void testVoid();
  
  @Async
  public String testLongTimeJob();
}
