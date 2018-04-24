package com.elminster.easy.rpc.server.processor.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.thread.IJobMonitor;
import com.elminster.common.thread.Job;
import com.elminster.common.threadpool.ThreadPool;
import com.elminster.common.util.ArrayUtil;
import com.elminster.common.util.ReflectUtil;
import com.elminster.common.util.TypeUtil;
import com.elminster.common.util.TypeUtil.CompactedType;
import com.elminster.easy.rpc.call.ReturnResult;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.Status;
import com.elminster.easy.rpc.call.impl.ReturnResultImpl;
import com.elminster.easy.rpc.context.InvokeContext;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.connection.impl.NioRpcCall;
import com.elminster.easy.rpc.server.connection.impl.NioRpcConnection;
import com.elminster.easy.rpc.server.container.worker.impl.WorkerJobId;
import com.elminster.easy.rpc.server.listener.RpcProcessEvent;
import com.elminster.easy.rpc.server.listener.RpcServerListener;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;
import com.elminster.easy.rpc.service.RpcService;

abstract public class RpcServiceProcessorBase implements RpcServiceProcessor {

  private static final Logger logger = LoggerFactory.getLogger(RpcServiceProcessorBase.class);
  protected final RpcServer rpcServer;
  protected final Map<String, RpcCall> unproccessedRpcCalls = new ConcurrentHashMap<>();
  protected final Map<String, RpcCall> processingRpcCalls = new ConcurrentHashMap<>();
  protected final Map<String, ProcessWorkJob> processingJobs = new ConcurrentHashMap<>();
  protected final BlockingQueue<RpcCall> queueForProcessing;
  protected final ConcurrentHashMap<String, RpcCall> processedRpcCalls = new ConcurrentHashMap<>();
  protected final ThreadPool threadPool;
  protected final ProcessWorker worker;

  public RpcServiceProcessorBase(RpcServer rpcServer) {
    this.rpcServer = rpcServer;
    int queueSize = rpcServer.getContext().getProcessorQueueSize();
    queueForProcessing = new PriorityBlockingQueue<>(queueSize, new RpcCallComparator());
    threadPool = new ThreadPool(rpcServer.getContext().getProcessingThreadPoolConfiguration());
    worker = new ProcessWorker();
    threadPool.execute(worker);
  }

  protected RpcService getRpcService(RpcCall rpcCall) throws RpcException {
    String serviceName = rpcCall.getServiceName();
    InvokeContext context = rpcCall.getContext();
    String methodName = rpcCall.getMethodName();

    RpcService service = null;
    service = rpcServer.getService(serviceName);
    if (null == service) {
      throw new RpcException(String.format("Service [%s] is NOT found! Context: [%s].", serviceName, context));
    }

    if (!ArrayUtil.contains(service.getServiceMethods(), methodName)) {
      throw new RpcException(String.format("Method [%s] is NOT pubulished in Service [%s]! Context: [%s]", methodName, serviceName, context));
    }
    return service;
  }

  protected RpcCall invokeInternal(RpcService service, RpcCall rpcCall) throws RpcException {
    rpcCall.setStatus(Status.PROCESSING);
    beforeProcess(rpcCall);
    String methodName = rpcCall.getMethodName();
    InvokeContext context = rpcCall.getContext();
    Object[] args = rpcCall.getArgs();
    try {
      if (logger.isDebugEnabled()) {
        logger.debug(String.format("Before Inovking RPC [%s].", rpcCall.toString()));
      }
      rpcCall.setInvokeStartAt(System.currentTimeMillis());
      Method method = ReflectUtil.getDeclaredMethod(service.getClass(), methodName, args);
      if (null == method) {
        throw new NoSuchMethodException();
      }
      final Object rtn = ReflectUtil.invoke(service, method, args);
      final CompactedType ct = TypeUtil.getMethodReturnTypeClass(service.getClass(), method);
      ReturnResult result = new ReturnResultImpl(ct.getType(), rtn);
      rpcCall.setResult(result);
      rpcCall.setInvokeEndAt(System.currentTimeMillis());
      if (logger.isDebugEnabled()) {
        logger.debug(String.format("After Invoking RPC [%s].", rpcCall.toString()));
      }
      rpcCall.setStatus(Status.PROCESSED);
      return rpcCall;
    } catch (NoSuchMethodException e) {
      rpcCall.setStatus(Status.EXCEPTION);
      throw new RpcException(String.format("Method [%s] is NOT found in Service [%s]! Context: [%s]", methodName, service, context), e);
    } catch (IllegalAccessException e) {
      rpcCall.setStatus(Status.EXCEPTION);
      throw new RpcException(String.format("Method [%s]'s access are illegal in Service [%s]! Context: [%s]", methodName, service, context), e);
    } catch (IllegalArgumentException e) {
      rpcCall.setStatus(Status.EXCEPTION);
      throw new RpcException(String.format("Method [%s]'s arguments are illegal in Service [%s]! Context: [%s]", methodName, service, context), e);
    } catch (InvocationTargetException e) {
      rpcCall.setStatus(Status.EXCEPTION);
      setException2Result(rpcCall, e.getTargetException());
      if (logger.isDebugEnabled()) {
        logger.debug(String.format("Exception on Invoking RPC [%s].", rpcCall.toString()));
      }
      return rpcCall;
    } finally {
      afterProcess(rpcCall);
    }

  }

  private RpcCall setException2Result(RpcCall rpcCall, final Throwable ex) {
    ReturnResult result = new ReturnResultImpl(ex.getClass(), ex);
    rpcCall.setResult(result);
    rpcCall.setInvokeEndAt(System.currentTimeMillis());
    return rpcCall;
  }

  protected void putProcessedCall(final RpcCall call) {
    String requestId = call.getRequestId();
    processingRpcCalls.remove(requestId);
    processingJobs.remove(requestId);
    processedRpcCalls.put(requestId, call);
    if (call instanceof NioRpcCall) {
      NioRpcCall nioCall = (NioRpcCall) call;
      if (!call.isAsyncCall()) {
        NioRpcConnection conn = nioCall.getConnection();
        conn.addFinishedCall(call);
      }
    }
  }

  protected void beforeProcess(RpcCall rpcCall) {
    for (RpcServerListener listener : rpcServer.getServerListeners()) {
      listener.preProcess(new RpcProcessEvent(rpcCall.getRequestId(), rpcCall.getServiceName(), rpcCall.getMethodName(), rpcCall.getArgs(), rpcCall.getContext()));
    }
  }

  protected void afterProcess(RpcCall rpcCall) {
    for (RpcServerListener listener : rpcServer.getServerListeners()) {
      listener.postProcess(
          new RpcProcessEvent(rpcCall.getRequestId(), rpcCall.getServiceName(), rpcCall.getMethodName(), rpcCall.getArgs(), rpcCall.getResult(), rpcCall.getContext()));
    }
  }

  class ProcessWorker extends Job {

    public ProcessWorker() {
      super(WorkerJobId.PROCESS_JOB.getJobId(), "Process Job");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JobStatus doWork(IJobMonitor monitor) throws Throwable {
      monitor.beginJob(this.getName(), 1);
      while (!Thread.currentThread().isInterrupted() && !monitor.isCancelled()) {
        RpcCall rpcCall = queueForProcessing.take();
        String reqeustId = rpcCall.getRequestId();
        unproccessedRpcCalls.remove(reqeustId);
        ProcessWorkJob processWorkJob = new ProcessWorkJob(rpcCall);
        threadPool.execute(processWorkJob);
        processingJobs.put(reqeustId, processWorkJob);
        processingRpcCalls.put(reqeustId, rpcCall);
      }
      return monitor.done();
    }
  }

  class ProcessWorkJob extends Job {

    private RpcCall rpcCall;

    public ProcessWorkJob(RpcCall rpcCall) {
      super(WorkerJobId.PROCESS_WORKER.getJobId(), "Process Worker Job");
      this.rpcCall = rpcCall;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JobStatus doWork(IJobMonitor monitor) throws Throwable {
      monitor.beginJob(this.getName(), 1);
      RpcService rpcService = null;
      RpcCall call = null;
      try {
        if (!monitor.isCancelled()) {
          rpcService = getRpcService(rpcCall);
          call = invokeInternal(rpcService, rpcCall);
        }
      } catch (RpcException rpce) {
        call = setException2Result(rpcCall, rpce);
      }
      putProcessedCall(call);
      return monitor.done();
    }
  }

  public void close() {
    worker.cancel();
    threadPool.shutdown();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcCall getRpcCall(String requestId) {
    RpcCall rpcCall = unproccessedRpcCalls.get(requestId);
    if (null == rpcCall) {
      rpcCall = processingRpcCalls.get(requestId);
    }
    if (null == rpcCall) {
      rpcCall = processedRpcCalls.get(requestId);
    }
    return rpcCall;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean cancelRpcCall(String requestId) {
    logger.debug(String.format("Cancel RPC Call [%s].", requestId));
    RpcCall rpcCall = getRpcCall(requestId);
    // remove unprocessed
    boolean removed = queueForProcessing.remove(rpcCall);
    // cancel processing
    Job job = processingJobs.get(requestId);
    if (null != job) {
      job.cancel();
      removed = true;
    }
    if (removed) {
      rpcCall.setStatus(Status.CANCELLED);
    }
    return removed;
  }
}
