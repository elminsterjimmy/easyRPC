package com.elminster.easy.rpc.it.nio;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
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
import com.elminster.easy.rpc.it.RpcTestIfClient;
import com.elminster.easy.rpc.it.RpcTestServiceImpl;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.context.impl.RpcServerContext;
import com.elminster.easy.rpc.server.exception.ServerException;
import com.elminster.easy.rpc.server.impl.RpcServerFactoryImpl;
import com.elminster.easy.rpc.server.listener.RpcProcessEvent;
import com.elminster.easy.rpc.server.listener.RpcServerAcceptEvent;
import com.elminster.easy.rpc.server.listener.RpcServerListenEvent;
import com.elminster.easy.rpc.server.listener.RpcServerListener;

public class TestNioRpcCommunication {

  private static final int CLIENT_COUNT = 100;
  private AtomicInteger accepted = new AtomicInteger(0);

  @BeforeClass
  public static void initLog4j() {
    DOMConfigurator.configure("log4j.xml");
  }

  @Test
  public void testNioRpcCommunication() throws ServerException, RpcException {
    RpcContext serverContext = createRpcServerContext();

    final RpcServer rpcServer = RpcServerFactoryImpl.INSTANCE.createRpcServer(serverContext);
    rpcServer.addService(new RpcTestServiceImpl());
    rpcServer.listen(9200);

    waitServerUp(rpcServer);

    final CountDownLatch latch = new CountDownLatch(CLIENT_COUNT);

    ClientThread[] clients = new ClientThread[CLIENT_COUNT];
    for (int i = 0; i < clients.length; i++) {
      clients[i] = new ClientThread(latch, "client" + i);
    }

    for (int i = 0; i < clients.length; i++) {
      clients[i].start();
    }

    try {
      latch.await(360, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      ;
    }
    if (latch.getCount() > 0) {
      System.out.println("Latch count: " + latch.getCount());
      Assert.fail("timeout!");
    }
    
    System.out.println(accepted.get());
    for (ClientThread client : clients) {
      if (null != client.e) {
        client.e.printStackTrace();
        Assert.fail(client.e.getMessage());
      }
    }
    rpcServer.shutdown(true);
  }

  class ClientThread extends Thread {

    CountDownLatch latch;
    Throwable e;

    public ClientThread(CountDownLatch latch, String name) {
      this.latch = latch;
      this.setName(name);
    }

    public void run() {
      RpcClient rpcClient = null;
      Random random = new Random();
      try {
        RpcContext clientContext = createRpcClientContext();
        ConnectionEndpoint endpoint = SimpleConnectionEndpoint.localhostConnectionEndpoint(9200);
        rpcClient = RpcClientFactoryImpl.INSTANCE.createRpcClient(endpoint, clientContext, 0 == random.nextInt(10) % 2);

        RpcProxy proxy = new DynamicProxy();
        RpcTestIfClient testIf = proxy.makeProxy(RpcTestIfClient.class, rpcClient);
        String uuid = UUID.randomUUID().toString();
        String helloWord = testIf.testString("world: " + uuid);
        Assert.assertEquals("hello world: " + uuid, helloWord);
        Assert.assertEquals(new Integer(0), (Integer) testIf.testIntegerPlus(null));
        Assert.assertEquals(6, testIf.testIntPlus(5));
        Assert.assertEquals(101, testIf.testLongPlus(100L));
        Assert.assertEquals(Integer.MIN_VALUE, testIf.testIntPlus(Integer.MAX_VALUE));

        Future<String> future = testIf.testLongTimeJob();
        Assert.assertEquals(false, future.isDone());
        Assert.assertEquals(false, future.isCancelled());
        
        Assert.assertTrue(testIf.now().getTime() - System.currentTimeMillis() < 1000);
        testIf.testVoid();
        try {
          String rtn = future.get();
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < 500; i++) {
            sb.append(String.valueOf(i));
          }
          String expect = sb.toString();
          
          Assert.assertEquals(expect.length(), rtn.length());
          Assert.assertArrayEquals(expect.toCharArray(), rtn.toCharArray());
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (ExecutionException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        try {
          testIf.unpublished();
          Assert.fail();
        } catch (Exception e) {
        }

      } catch (Throwable e) {
        this.e = e;
      } finally {
        if (null != rpcClient) {
          rpcClient.disconnect();
        }
        latch.countDown();
      }

    }
  }

  private void waitServerUp(final RpcServer rpcServer) {
    
    rpcServer.addServerListener(new RpcServerListener() {

      @Override
      public void beforeServe(RpcServerListenEvent event) {
      }

      @Override
      public void beforeUnserve(RpcServerListenEvent event) {

      }

      @Override
      public void afterListened(RpcServerListenEvent event) {
        System.out.println("Server's up and listened on " + event.getHost() + ":" + event.getPort());
      }

      @Override
      public void preProcess(RpcProcessEvent event) {
      }

      @Override
      public void postProcess(RpcProcessEvent event) {
      }

      @Override
      public void onAccept(RpcServerAcceptEvent event) {
        accepted.getAndIncrement();
      }
    });
    
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private RpcContext createRpcClientContext() {
    return RpcClientContext.createBioClientContext();
  }

  private RpcContext createRpcServerContext() {
    return RpcServerContext.createNioServerContext();
  }
}
