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
import com.elminster.easy.rpc.server.container.worker.ContainerWorker;

public class ContainerReader extends Job implements ContainerWorker {

  private static final AtomicInteger READER_SERIAL = new AtomicInteger(WorkerJobId.NIO_READ_WORKER.getJobId());

  private static final Logger logger = LoggerFactory.getLogger(ContainerReader.class);

  private final Selector selector;

  public ContainerReader(Selector selector) {
    super(READER_SERIAL.get(), "Nio Container Reader - " + (READER_SERIAL.get()));
    this.selector = selector;
    READER_SERIAL.incrementAndGet();
  }

  @Override
  protected JobStatus doWork(IJobMonitor monitor) throws Throwable {
    try {
      while (!monitor.isCancelled()) {
        try {
          if (selector.select(100) == 0) {
            continue;
          }
          Iterator<SelectionKey> selecionKeys = selector.selectedKeys().iterator();

          while (selecionKeys.hasNext()) {
            SelectionKey key = selecionKeys.next();
            selecionKeys.remove();

            if (!key.isValid()) {
              continue;
            }

            if (key.isReadable()) {
              NioRpcConnection connection = (NioRpcConnection) key.attachment();
              connection.run();
              JobStatus jobStatus = connection.getJobStatus();
              if (JobStatus.ERROR == jobStatus) {
                ////// error happened
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

  private void cleanup() {
    try {
      selector.close();
    } catch (IOException e) {
      logger.warn(String.format("Failed to cleanup the selector[%s]", selector.toString(), e));
    }
  }

  public void registerChannel(SocketChannel socketChannel, NioRpcConnection connection) throws ClosedChannelException {
    socketChannel.register(selector, SelectionKey.OP_READ, connection);
  }

  public void awakeSelector() {
    selector.wakeup();
  }

}
