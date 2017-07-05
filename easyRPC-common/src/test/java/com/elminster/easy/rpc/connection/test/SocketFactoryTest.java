package com.elminster.easy.rpc.connection.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.threadpool.ThreadPoolConfiguration;
import com.elminster.easy.rpc.codec.CoreCodec;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.connection.SocketFactory;
import com.elminster.easy.rpc.connection.impl.NIOSocketFactoryImpl;
import com.elminster.easy.rpc.connection.impl.StreamSocketFactoryImpl;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.registery.SocketFactoryRegsitery;

public class SocketFactoryTest {

  private ExecutorService executor = Executors.newFixedThreadPool(10);

  @Ignore
  @Test
  public void testStreamSocketFactoryWithoutSecure() throws IOException, ObjectInstantiationExcption {
    SocketFactory socketFactory = setupStreamSocketFactory();

    final CountDownLatch latch = new CountDownLatch(2);
    final int port = 9001;

    // test without secure
    final ServerSocket serverSocket = socketFactory.createServerSocket(port, false);
    ConnectionEndpoint endpoint = new ConnectionEndpoint() {

      @Override
      public Boolean useSecureSocket() {
        return false;
      }

      @Override
      public Integer getPort() {
        return port;
      }

      @Override
      public String getHost() {
        return "localhost";
      }
    };
    final Socket clientSocket = socketFactory.createClientSocket(endpoint);

    executor.execute(new Runnable() {

      @Override
      public void run() {
        try {
          Socket socket = serverSocket.accept();
          try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
            CoreCodec rpcUtil = CoreCodecFactory.INSTANCE.getCoreCodec(in, out);
            String str = rpcUtil.readStringAsciiNullable();
            if ("hello".equals(str)) {
              rpcUtil.writeStringAsciiNullable("bye");
            } else if ("bye".equals(str)) {
              serverSocket.close();
            } else {
              Assert.fail("unexcpeted result.");
            }
          }
        } catch (IOException e) {
          Assert.fail(e.getMessage());
        } finally {
          latch.countDown();
        }
      }
    });

    executor.execute(new Runnable() {

      @Override
      public void run() {
        try (InputStream in = clientSocket.getInputStream(); OutputStream out = clientSocket.getOutputStream()) {
          CoreCodec rpcUtil = CoreCodecFactory.INSTANCE.getCoreCodec(in, out);
          rpcUtil.writeStringAsciiNullable("hello");
          if ("bye".equals(rpcUtil.readStringAsciiNullable())) {
            rpcUtil.writeStringAsciiNullable("bye");
            clientSocket.close();
          } else {
            Assert.fail("unexcpeted result.");
          }
        } catch (IOException e) {
          Assert.fail(e.getMessage());
        } finally {
          latch.countDown();
        }
      }

    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Ignore
  @Test
  public void testStreamSocketFactoryWithSecure() throws IOException, ObjectInstantiationExcption {
    SocketFactory socketFactory = setupStreamSocketFactory();

    final CountDownLatch latch = new CountDownLatch(2);
    final int port = 9002;

    // test without secure
    final ServerSocket serverSocket = socketFactory.createServerSocket(port, true);
    ConnectionEndpoint endpoint = new ConnectionEndpoint() {

      @Override
      public Boolean useSecureSocket() {
        return true;
      }

      @Override
      public Integer getPort() {
        return port;
      }

      @Override
      public String getHost() {
        return "localhost";
      }
    };
    final Socket clientSocket = socketFactory.createClientSocket(endpoint);

    executor.execute(new Runnable() {

      @Override
      public void run() {
        try {
          Socket socket = serverSocket.accept();
          try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
            CoreCodec rpcUtil = CoreCodecFactory.INSTANCE.getCoreCodec(in, out);
            if ("hello".equals(rpcUtil.readStringAsciiNullable())) {
              rpcUtil.writeStringAsciiNullable("bye");
            } else if ("bye".equals(rpcUtil.readStringAsciiNullable())) {
              serverSocket.close();
            } else {
              Assert.fail("unexcpeted result.");
            }
          }
        } catch (IOException e) {
          Assert.fail(e.getMessage());
        } finally {
          latch.countDown();
        }
      }
    });

    executor.execute(new Runnable() {

      @Override
      public void run() {
        try (InputStream in = clientSocket.getInputStream(); OutputStream out = clientSocket.getOutputStream()) {
          CoreCodec rpcUtil = CoreCodecFactory.INSTANCE.getCoreCodec(in, out);
          rpcUtil.writeStringAsciiNullable("hello");
          String str = rpcUtil.readStringAsciiNullable();
          if ("bye".equals(str)) {
            rpcUtil.writeStringAsciiNullable("bye");
            clientSocket.close();
          } else {
            Assert.fail("unexcpeted result.");
          }
        } catch (IOException e) {
          Assert.fail(e.getMessage());
        } finally {
          latch.countDown();
        }
      }

    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      Assert.fail(e.getMessage());
    }
  }

  private SocketFactory setupStreamSocketFactory() throws ObjectInstantiationExcption {
    RpcContext context = new RpcContextAdapter() {

      @Override
      public String getSocketFactoryClassName() {
        return StreamSocketFactoryImpl.class.getName();
      }
    };
    return SocketFactoryRegsitery.INSTANCE.getSocketFactory(context);
  }

  @Ignore
  @SuppressWarnings("restriction")
  @Test
  public void testNIOSocketFactory() throws IOException, ObjectInstantiationExcption {
    SocketFactory socketFactory = setupNIOSocketFactory();

    final CountDownLatch latch = new CountDownLatch(2);

    ServerSocket serverSocket = socketFactory.createServerSocket(9005, false);
    final ServerSocketChannel serverChannel = serverSocket.getChannel();

    executor.execute(new Runnable() {

      @Override
      public void run() {
        Selector selector;
        try {
          selector = Selector.open();
          serverChannel.register(selector, SelectionKey.OP_ACCEPT);

          while (true) {
            if (0 == selector.select(100)) {
              continue;
            }

            Iterator<SelectionKey> selecionKeys = selector.selectedKeys().iterator();

            while (selecionKeys.hasNext()) {
              SelectionKey selctionKey = selecionKeys.next();
              // remove the key otherwise the key will be rereaded.
              selecionKeys.remove();

              if (!selctionKey.isValid()) {
                cleanupSelctionKey(selctionKey);
              }
              if (selctionKey.isAcceptable()) {
                System.out.println("accpet...");
                handleAccept(selctionKey);
              } else if (selctionKey.isConnectable()) {
                System.out.println("connecting...");
              } else if (selctionKey.isReadable()) {
                System.out.println("readable...");
                handleRead(selctionKey, serverChannel);
              } else if (selctionKey.isWritable()) {
                System.out.println("writable...");
                handleWrite(selctionKey);
              }
            }
          }
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } finally {
          latch.countDown();
        }
      }

      private void cleanupSelctionKey(SelectionKey selctionKey) {
        selctionKey.cancel();
      }

      public void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        System.out.println("Server: accept client socket " + socketChannel);
        socketChannel.configureBlocking(false);
        socketChannel.register(key.selector(), SelectionKey.OP_READ);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        key.attach(byteBuffer);
      }

      public void handleRead(SelectionKey key, ServerSocketChannel serverChannel) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        CoreCodec util = CoreCodecFactory.INSTANCE.getCoreCodec(socketChannel);
        String str = util.readStringAsciiNullable();
        System.out.println(str);
        if ("hello".equals(str)) {
          util.writeStringAsciiNullable("bye");
          System.out.println("wrote bye");
        } else if ("bye".equals(str)) {
          serverChannel.close();
        } else {
          Assert.fail("Unexpected result." + str);
        }
      }

      public void handleWrite(SelectionKey key) throws IOException {
        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
        byteBuffer.flip();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.write(byteBuffer);
        if (byteBuffer.hasRemaining()) {
          key.interestOps(SelectionKey.OP_READ);
        }
        byteBuffer.compact();
      }
    });

    final Socket clientSocket = socketFactory.createClientSocket(new ConnectionEndpoint() {

      @Override
      public Boolean useSecureSocket() {
        return false;
      }

      @Override
      public Integer getPort() {
        return 9005;
      }

      @Override
      public String getHost() {
        return "localhost";
      }
    });

    final SocketChannel socketChannel = clientSocket.getChannel();
    executor.execute(new Runnable() {

      @Override
      public void run() {
        try {
          while (!socketChannel.finishConnect())
            ;
          CoreCodec util = CoreCodecFactory.INSTANCE.getCoreCodec(socketChannel);
          util.writeStringAsciiNullable("hello");
          String str = util.readStringAsciiNullable();
          System.out.println(str);
          if (null != str) {
            if ("bye".equals(str)) {
              util.writeStringAsciiNullable("bye");
              clientSocket.close();
            } else {
              Assert.fail("unexcpeted result." + str);
            }
          }
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } finally {
          latch.countDown();
        }
      }

    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private SocketFactory setupNIOSocketFactory() throws ObjectInstantiationExcption {
    RpcContext context = new RpcContextAdapter() {

      @Override
      public String getSocketFactoryClassName() {
        return NIOSocketFactoryImpl.class.getName();
      }
    };
    SocketFactory socketFactory = SocketFactoryRegsitery.INSTANCE.getSocketFactory(context);
    return socketFactory;
  }

  public static boolean isReadyForRead(SocketChannel socket) throws IOException {
    return isReady(socket, SelectionKey.OP_READ);
  }

  public static boolean isReadyForWrite(SocketChannel socket) throws IOException {
    return isReady(socket, SelectionKey.OP_WRITE);
  }

  public static boolean isReady(SocketChannel socket, int op) throws IOException {
    // Setup
    if (socket.isBlocking())
      throw new IllegalArgumentException("Socket must be in non-blocking mode");

    Selector selector = SelectorProvider.provider().openSelector();
    socket.register(selector, op);

    // Real work
    if (selector.selectNow() == 0)
      return false;
    // Just in case selector has other keys
    return selector.selectedKeys().contains(socket.keyFor(selector));
  }

  class RpcContextAdapter implements RpcContext {

    @Override
    public String getServerContainerClassName() {
      return null;
    }

    @Override
    public String getSocketFactoryClassName() {
      return null;
    }

    @Override
    public Integer getClientTimeout() {
      return null;
    }

    @Override
    public Boolean getClientTcpNoDelay() {
      return null;
    }

    @Override
    public String getServiceProcessorClassName() {
      return null;
    }

    @Override
    public String getClientContainerClassName() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ThreadPoolConfiguration getWorkerThreadPoolConfiguration() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Integer getReaderWorkerCount() {
      // TODO Auto-generated method stub
      return null;
    }
  }
}
