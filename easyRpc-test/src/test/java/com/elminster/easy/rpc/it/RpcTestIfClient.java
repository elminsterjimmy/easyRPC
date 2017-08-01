package com.elminster.easy.rpc.it;

import java.sql.Timestamp;
import java.util.concurrent.Future;

import com.elminster.easy.rpc.idl.Async;
import com.elminster.easy.rpc.service.Rpc;

@Rpc("Test")
public interface RpcTestIfClient {

  public String testString(String world);
  
  public int testIntPlus(int i);
  
  public Integer testIntegerPlus(Integer i);
  
  public long testLongPlus(long l);

  public Timestamp now();
  
  public String unpublished();
  
  public void testVoid();
  
  @Async
  public Future<String> testLongTimeJob();
}
