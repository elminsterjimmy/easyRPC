package com.elminster.easy.rpc.server.container.worker.impl;

import java.io.IOException;
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
import com.elminster.easy.rpc.server.connection.impl.NioRpcConnection;
import com.elminster.easy.rpc.server.container.impl.NioContainer;
import com.elminster.easy.rpc.server.container.worker.ContainerWorker;

/**
 * The NIO container reader.
 * 
 * @author jinggu
 * @version 1.0
 */
public class NioContainerReader extends Job implements ContainerWorker {

  private static final AtomicInteger READER_SERIAL = new AtomicInteger(WorkerJobId.NIO_READ_WORKER.getJobId());

  private static final Logger logger = LoggerFactory.getLogger(NioContainerReader.class);

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
        if (selector.select(100) == 0) {
          continue;
        }
        Iterator<SelectionKey> selecionKeys = selector.selectedKeys().iterator();

        while (selecionKeys.hasNext()) {
          SelectionKey key = selecionKeys.next();
          selecionKeys.remove();

          if (!key.isValid()) {
            key.attach(null);
            continue;
          }

          if (key.isReadable()) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            NioRpcConnection conn = container.getConnection(socketChannel);
            conn.run();
          }
        }
      }
      return monitor.done();
    } finally {
      cleanup();
    }
  }

  private void cleanup() {
    logger.debug("Cleanup container reader [{}].", this.getName());
    try {
      selector.close();
    } catch (IOException e) {
      logger.warn(String.format("Failed to cleanup the selector[%s]", selector.toString(), e));
    }
  }

  public void registerChannel(SocketChannel socketChannel) throws ClosedChannelException {
    socketChannel.register(selector, SelectionKey.OP_READ);
  }
}
