package com.elminster.easy.rpc.server.container.worker.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.thread.IJobMonitor;
import com.elminster.common.thread.Job;
import com.elminster.easy.rpc.server.buffer.BufferPool;
import com.elminster.easy.rpc.server.connection.impl.NioRpcConnection;
import com.elminster.easy.rpc.server.container.impl.NioContainer;
import com.elminster.easy.rpc.server.container.worker.ContainerWorker;
import com.elminster.easy.rpc.util.NioChannelUtil;

/**
 * The NIO container reader.
 * 
 * @author jinggu
 * @version 1.0
 */
public class NioContainerReader extends Job implements ContainerWorker {

  private static final AtomicInteger READER_SERIAL = new AtomicInteger(WorkerJobId.NIO_READ_WORKER.getJobId());
  
  private static final Logger logger = LoggerFactory.getLogger(NioContainerReader.class);
  
  private ThreadLocal<BufferPool> bufferPool = new ThreadLocal<BufferPool>() {
    protected BufferPool initialValue() {
      return new BufferPool();
    }
  };

  private final Selector selector;
  private final NioContainer container;
  
  {
    READER_SERIAL.getAndIncrement();
  }

  public NioContainerReader(Selector selector, NioContainer container) {
    super(READER_SERIAL.get(), "Nio Container Reader - " + Integer.toHexString(READER_SERIAL.get()));
    this.selector = selector;
    this.container = container;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected JobStatus doWork(IJobMonitor monitor) throws Throwable {
    monitor.beginJob(this.getName(), 1);
    try {
      while (!monitor.isCancelled()) {
        try {
          if (selector.select(100) == 0) {
            continue;
          }
          Iterator<SelectionKey> selecionKeys = selector.selectedKeys().iterator();
          
          System.err.println(selector.selectedKeys().size());

          while (selecionKeys.hasNext()) {
            SelectionKey key = selecionKeys.next();
            selecionKeys.remove();

            if (!key.isValid()) {
              key.attach(null);
              continue;
            }

            if (key.isReadable()) {
              SocketChannel socketChannel = (SocketChannel) key.channel();
              Object attch = key.attachment();
              if (attch instanceof Attachment) {
                Attachment attachment = (Attachment) attch;
                read(socketChannel, key, attachment.bufferType, attachment.byteBuffer);
              } else {
                ByteBuffer headBuffer = bufferPool.get().get(5);
                read(socketChannel, key, 1, headBuffer);
              }
            }
          }
        } catch (IOException ioe) {
          ;
        }
      }
      return monitor.done();
    } finally {
      cleanup();
    }
  }
  
  private void read(SocketChannel socketChannel, SelectionKey key, int bufferType, ByteBuffer byteBuffer) throws IOException {
    int dataSize = byteBuffer.remaining();
    int read = socketChannel.read(byteBuffer);
    if (1 == bufferType) {
      if (read < dataSize) {
        Attachment attachment = new Attachment();
        attachment.bufferType = 1;
        attachment.byteBuffer = byteBuffer;
        key.attach(attachment);
        key.interestOps(SelectionKey.OP_READ);
      } else {
        byteBuffer.rewind();
        byteBuffer.get();
        dataSize = byteBuffer.getInt();
        
        // read data
        ByteBuffer dataBuffer = bufferPool.get().get(dataSize + byteBuffer.limit());
        byteBuffer.rewind();
        dataBuffer.put(byteBuffer);
        bufferPool.get().release(byteBuffer);
        read(socketChannel, key, 2, dataBuffer);
      }
    } else if (2 == bufferType) {
      if (read < dataSize) {
        Attachment attachment = new Attachment();
        attachment.bufferType = 2;
        attachment.byteBuffer = byteBuffer;
        key.attach(attachment);
        key.interestOps(SelectionKey.OP_READ);
      } else {
        // TODO send buffer to connection
        byte[] bytes = new byte[byteBuffer.limit()];
        byteBuffer.rewind();
        byteBuffer.get(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
          sb.append(String.format("%3d", b) + "|");
        }
        System.err.println(sb.toString());
      }
    }
  }

  class Attachment {
    int bufferType;
    ByteBuffer byteBuffer;
  }

  private void cleanup() {
    logger.debug("Cleanup container reader [{}].", this.getName());
    try {
      selector.close();
    } catch (IOException e) {
      logger.warn(String.format("Failed to cleanup the selector[%s]", selector.toString(), e));
    }
  }

  public void registerChannel(SocketChannel socketChannel, NioRpcConnection connection) throws ClosedChannelException {
    socketChannel.register(selector, SelectionKey.OP_READ, connection);
  }
}
