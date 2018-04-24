package com.elminster.easy.rpc.server.container.worker.impl;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.thread.IJobMonitor;
import com.elminster.common.thread.Job;
import com.elminster.easy.rpc.server.connection.impl.NioRpcConnection;
import com.elminster.easy.rpc.server.container.impl.NioContainer;
import com.elminster.easy.rpc.server.container.worker.ContainerWorker;

public class NioContainerWriter extends Job implements ContainerWorker {
  
  private static final Logger logger = LoggerFactory.getLogger(NioContainerWriter.class);

  private final Selector selector;
  private final NioContainer container;

  public NioContainerWriter(Selector selector, NioContainer container) {
    super(WorkerJobId.NIO_WRITE_WORKER.getJobId(), "Nio Container Writer");
    this.selector = selector;
    this.container = container;
  }

  @Override
  protected JobStatus doWork(IJobMonitor monitor) throws Throwable {
    monitor.beginJob(this.getName(), 1);
    try {
      while (!monitor.isCancelled()) {
        try {
          selector.select(10);
          Iterator<SelectionKey> selecionKeys = selector.selectedKeys().iterator();

          while (selecionKeys.hasNext()) {
            SelectionKey key = selecionKeys.next();
            selecionKeys.remove();

            if (!key.isValid()) {
              continue;
            }

            if (key.isWritable()) {
              SocketChannel socketChannel = (SocketChannel) key.channel();
              NioRpcConnection conn = container.getConnection(socketChannel);
              try {
                conn.write();
              } catch (Exception e) {
                conn.close();
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
  
  public SelectionKey registerChannel(SocketChannel socketChannel) throws ClosedChannelException {
    logger.debug("registerWriterChannel="+socketChannel);
    SelectionKey key = socketChannel.register(selector, SelectionKey.OP_WRITE);
    selector.wakeup();
    return key;
  }
  
  private void cleanup() {
    logger.debug("Cleanup container writer [{}].", this.getName());
    try {
      selector.close();
    } catch (IOException e) {
      logger.warn(String.format("Failed to cleanup the selector[%s]", selector.toString(), e));
    }
  }
  
  public Selector getSelector() {
    return selector;
  }
}