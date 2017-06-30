package com.elminster.easy.rpc.server.test;

import org.junit.Test;

import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.server.RpcServer;

public class RpcServerTest {

  @Test
  public void testRpcServer() {
    RpcServer server = createRpcServer();
  }

  private RpcServer createRpcServer() {
    
    
    // TODO Auto-generated method stub
    return null;
  }
  
  private RpcContext createRpcContext() {
    
    return new RpcContext() {
      
      @Override
      public String getSocketFactoryClassName() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String getServiceProcessorClassName() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String getServerListenerClassName() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String getServerContainerClassName() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Integer getClientTimeout() {
        return 0;
      }
      
      @Override
      public Boolean getClientTcpNoDelay() {
        // TODO Auto-generated method stub
        return false;
      }
    }; 
  }
}
