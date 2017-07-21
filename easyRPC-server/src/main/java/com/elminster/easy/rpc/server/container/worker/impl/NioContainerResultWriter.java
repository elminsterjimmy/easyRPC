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
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.server.connection.impl.NioRpcCall;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.worker.ContainerWorker;

public class NioContainerResultWriter extends Job implements ContainerWorker {
  
  private static final Logger logger = LoggerFactory.getLogger(NioContainerResultWriter.class);

  private final Selector selector;
  private final Container container;

  public NioContainerResultWriter(Selector selector, Container container) {
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

            if (key.isWritable()) {
              RpcCall rpcCall = container.getServiceProcessor().getResult();
              if (rpcCall instanceof NioRpcCall) {
                NioRpcCall nioRpcCall = (NioRpcCall) rpcCall;
                nioRpcCall.getConnection().writeResponse(nioRpcCall);
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
  
  public void registerChannel(SocketChannel socketChannel) throws ClosedChannelException {
    socketChannel.register(selector, SelectionKey.OP_WRITE);
  }
  
  public void awakeSelector() {
    selector.wakeup();
  }

  private void cleanup() {
    logger.debug("Cleanup container writer [{}].", this.getName());
    try {
      selector.close();
    } catch (IOException e) {
      logger.warn(String.format("Failed to cleanup the selector[%s]", selector.toString(), e));
    }
  }
}
