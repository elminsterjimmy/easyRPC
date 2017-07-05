package com.elminster.easy.rpc.server.container.impl;

import com.elminster.common.thread.IJobMonitor;
import com.elminster.common.thread.Job;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.listener.ServerListener;
import com.elminster.easy.rpc.server.container.listener.impl.BioServerListenerImpl;
import com.elminster.easy.rpc.server.container.worker.ContainerWorker;
import com.elminster.easy.rpc.server.container.worker.impl.WorkerJobId;

/**
 * The Socket Container.
 * 
 * @author jinggu
 * @version 1.0
 */
public class BioContainer extends ContainerBase implements Container {
  
  private ContainerWorker listenWorker;

  public BioContainer(RpcServer rpcServer, ConnectionEndpoint endpoint) {
    super(rpcServer, endpoint);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void startWorkerThreads() {
    ServerListener listener = new BioServerListenerImpl(rpcServer, this, endpoint);
    listenWorker = new ListenWorker(listener);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void serve() throws Exception {
    this.getAsyncWorkerThreadPool().execute(listenWorker);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void stopServe() throws Exception {
    listenWorker.cancel();
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
      super(WorkerJobId.BIO_LISTEN_WORKER.getJobId(), "Bio Container Listen Worker.");
      this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JobStatus doWork(IJobMonitor monitor) throws Throwable {
      try {
        setServing(true);
        listener.listen();
        return monitor.done();
      } finally {
        setServing(false);
        if (null != listener) {
          listener.close();
        }
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
