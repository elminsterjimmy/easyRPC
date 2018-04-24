package com.elminster.easy.rpc.server.container.impl;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.thread.IJobMonitor;
import com.elminster.common.thread.Job;
import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.connection.impl.NioRpcConnection;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.listener.ServerListener;
import com.elminster.easy.rpc.server.container.listener.impl.NioServerListenerImpl;
import com.elminster.easy.rpc.server.container.worker.ContainerWorker;
import com.elminster.easy.rpc.server.container.worker.impl.NioContainerReader;
import com.elminster.easy.rpc.server.container.worker.impl.NioContainerWriter;
import com.elminster.easy.rpc.server.container.worker.impl.WorkerJobId;

/**
 * The NIO container.
 * 
 * @author jinggu
 * @version 1.0
 */
public class NioContainer extends ContainerBase implements Container {

  /** the logger. */
  private static final Logger logger = LoggerFactory.getLogger(NioContainer.class);

  protected NioContainerReader[] readers;
  protected NioContainerWriter resultWriter;
  protected ListenWorker listenWorker;
  private int currentReader = 0;
  private Map<SocketChannel, NioRpcConnection> channel2ConnMap = new ConcurrentHashMap<>();
  
  
  public NioContainer(RpcServer rpcServer, ConnectionEndpoint endpoint) {
    super(rpcServer, endpoint);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void startWorkerThreads() throws Exception {
    RpcContext context = rpcServer.getContext();
    int readerWorkerCount = context.getReaderWorkerCount();
    try {
      ServerListener listener = new NioServerListenerImpl(rpcServer, this, endpoint);
      listenWorker = new ListenWorker(listener);

      readers = new NioContainerReader[readerWorkerCount];
      for (int i = 0; i < readerWorkerCount; i++) {
        Selector readerSelector = Selector.open();
        readers[i] = new NioContainerReader(readerSelector, this);
      }

      Selector writerSelector = Selector.open();
      resultWriter = new NioContainerWriter(writerSelector, this);
    } catch (IOException e) {
      throw e;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void serve() throws Exception {
    this.getAsyncWorkerThreadPool().execute(listenWorker);
    this.getAsyncWorkerThreadPool().execute(resultWriter);
    for (NioContainerReader reader : readers) {
      this.getAsyncWorkerThreadPool().execute(reader);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void stopServe() throws Exception {
    if (null != listenWorker) {
      listenWorker.cancel();
    }
    if (null != readers) {
      for (NioContainerReader reader : readers) {
        reader.cancel();
      }
    }
  }

  /**
   * Assign the connection to the reader.
   * 
   * @param connection
   *          the connection
   * @throws ClosedChannelException
   *           on error
   */
  public void assign2Reader(NioRpcConnection connection) throws ClosedChannelException {
    NioContainerReader reader = selectReader();
    logger.debug("Assign connection [{}] to Reader [{}].", connection.getName(), reader.getName());
    connection.registerReader(reader);
  }
  
  public void assign2Writer(NioRpcConnection connection) throws ClosedChannelException {
    connection.registerWriter(resultWriter);
  }
  
  /**
   * select a reader.
   * 
   * @return a reader
   */
  private NioContainerReader selectReader() {
    currentReader = ((currentReader++) % readers.length);
    return this.readers[currentReader];
  }
  
  public NioRpcConnection getConnection(SocketChannel socketChannel) {
    return this.channel2ConnMap.get(socketChannel);
  }
  
  @Override
  public void removeOpenConnection(RpcConnection connection) {
    super.removeOpenConnection(connection);
    NioRpcConnection nioConn = (NioRpcConnection) connection;
    channel2ConnMap.remove(nioConn.getSocketChannel());
  }

  @Override
  public void addOpenConnection(RpcConnection connection) {
    super.addOpenConnection(connection);
    NioRpcConnection nioConn = (NioRpcConnection) connection;
    channel2ConnMap.put(nioConn.getSocketChannel(), nioConn);
  }

  /**
   * The listen worker.
   * 
   * @author jinggu
   * @version 1.0
   */
  class ListenWorker extends Job implements ContainerWorker {

    private final ServerListener listener;

    public ListenWorker(ServerListener listener) {
      super(WorkerJobId.NIO_LISTEN_WORKER.getJobId(), "Nio Container Linsten Worker");
      this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JobStatus doWork(IJobMonitor monitor) throws Throwable {
      monitor.beginJob(this.getName(), 1);
      try {
        setServing(true);
        listener.listen();
        return monitor.done();
      } finally {
        listener.close();
        setServing(false);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel() {
      super.cancel();
      listener.interrupt();
    }
  }
}
