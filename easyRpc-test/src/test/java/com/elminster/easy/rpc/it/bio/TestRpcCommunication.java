package com.elminster.easy.rpc.it.bio;

import org.junit.Assert;
import org.junit.Test;

import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.client.context.impl.RpcClientContext;
import com.elminster.easy.rpc.client.impl.RpcClientFactoryImpl;
import com.elminster.easy.rpc.client.proxy.RpcProxy;
import com.elminster.easy.rpc.client.proxy.impl.DynamicProxy;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.context.impl.SimpleConnectionEndpoint;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.context.impl.RpcServerContext;
import com.elminster.easy.rpc.server.exception.ServerException;
import com.elminster.easy.rpc.server.impl.RpcServerFactoryImpl;
import com.elminster.easy.rpc.server.listener.RpcProcessEvent;
import com.elminster.easy.rpc.server.listener.RpcServerListenEvent;
import com.elminster.easy.rpc.server.listener.RpcServerListener;

public class TestRpcCommunication {

  @Test
  public void testBioRpcCommunication() throws ServerException, RpcException {
    RpcContext serverContext = createRpcServerContext();
    RpcContext clientContext = createRpcClientContext();

    ConnectionEndpoint endpoint = SimpleConnectionEndpoint.localhostConnectionEndpoint(9100);

    final RpcServer rpcServer = RpcServerFactoryImpl.INSTANCE.createRpcServer(serverContext);
    rpcServer.addService(new RpcTestServiceImpl());
    rpcServer.listen(9100);

    waitServerUp(rpcServer);

    RpcClient rpcClient = RpcClientFactoryImpl.INSTANCE.createRpcClient(endpoint, clientContext);

    RpcProxy proxy = new DynamicProxy();
    RpcTestIf testIf = proxy.makeProxy(RpcTestIf.class, rpcClient);

    String helloWord = testIf.testString("world");
    Assert.assertEquals("hello world", helloWord);

    Assert.assertEquals(new Integer(0), (Integer) testIf.testIntegerPlus(null));
    Assert.assertEquals(6, testIf.testIntPlus(5));
    Assert.assertEquals(101, testIf.testLongPlus(100L));

    Assert.assertEquals(Integer.MIN_VALUE, testIf.testIntPlus(Integer.MAX_VALUE));
    
    Assert.assertTrue(testIf.now().getTime() - System.currentTimeMillis() < 1000);
    
    try {
      testIf.unpublished();
      Assert.fail();
    } catch (Exception e) {
      e.printStackTrace();
    }

    rpcClient.disconnect();
    rpcServer.shutdown(true);
  }

  private void waitServerUp(final RpcServer rpcServer) {
    rpcServer.addServerListener(new RpcServerListener() {

      @Override
      public void beforeServe(RpcServerListenEvent event) {
      }

      @Override
      public void beforeClose(RpcServerListenEvent event) {

      }

      @Override
      public void afterListened(RpcServerListenEvent event) {
        System.out.println("Server's up and listened on " + event.getHost() + ":" + event.getPort());
        synchronized (rpcServer) {
          rpcServer.notify();
        }
      }

      @Override
      public void preProcess(RpcProcessEvent event) {
      }

      @Override
      public void postProcess(RpcProcessEvent event) {
      }
    });
    synchronized (rpcServer) {
      try {
        rpcServer.wait(60 * 1000l);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private RpcContext createRpcClientContext() {
    return RpcClientContext.createBioClientContext();
  }

  private RpcContext createRpcServerContext() {
    return RpcServerContext.createBioServerContext();
  }
}
