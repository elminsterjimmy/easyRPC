package com.elminster.easy.rpc.client.async;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.impl.ReturnResultImpl;
import com.elminster.easy.rpc.call.impl.RpcCallImpl;
import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.data.Async;
import com.elminster.easy.rpc.data.Request;
import com.elminster.easy.rpc.exception.ConnectionException;
import com.elminster.easy.rpc.exception.RpcException;

/**
 * The Async Future.
 * 
 * @author jinggu
 * @version 1.0
 */
public class AsyncFuture implements Future<Object> {

  private static final Logger logger = LoggerFactory.getLogger(AsyncFuture.class);
  protected final String requestId;
  protected final RpcCall rpcCall;
  protected final RpcClient client;
  protected Object result;
  protected Throwable exception;
  protected volatile boolean got = false;
  protected volatile boolean done = false;
  protected volatile boolean cancelled = false;
  // TODO reuse the future. protected transient boolean reuse = false;

  public AsyncFuture(RpcClient client, RpcCall rpcCall) {
    this.client = client;
    this.rpcCall = rpcCall;
    this.requestId = rpcCall.getRequestId();
  }
  
  synchronized protected void tryConnect() throws ConnectionException {
    if (!client.isConnected()) {
      this.client.connect();
      if (logger.isDebugEnabled()) {
        logger.debug("client connected!");
      }
    }
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    this.cancelled = true;
    if (!this.done) {
      try {
        tryConnect();
        RpcCall call = generateRpcCall("cancel", rpcCall.getRequestId());
        client.invokeService(call);
      } catch (Throwable e) {
        throw new RuntimeException(e);
      } finally {
        client.disconnect();
      }
      synchronized (this) {
        this.notify();
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public boolean isDone() {
    if (!this.done) {
      try {
        tryConnect();
        RpcCall call = generateRpcCall("isDone", rpcCall.getRequestId());
        Boolean isDone = (Boolean) client.invokeService(call);
        this.done = isDone.booleanValue();
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    }
    return this.done;
  }
  
  @Override
  public Object get() throws InterruptedException, ExecutionException {
    try {
      return get(0, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      throw new ExecutionException(e);
    }
  }

  @Override
  public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (this.done && this.got) {
      return this.result;
    }
    if (this.cancelled) {
      throw new InterruptedException(String.format("Request [%s] is Cancelled!", rpcCall.getRequestId()));
    }
    if (null != this.exception) {
      throw new ExecutionException(this.exception);
    }
    try {
      tryConnect();
      RpcCall call = generateRpcCall("get", rpcCall.getRequestId(), unit.toMillis(timeout));
      Object value = client.invokeService(call);
      if (value instanceof RpcException) {
        this.exception = (RpcException) value;
        throw new ExecutionException(this.exception);
      }
      
      // write rpcCall info
      rpcCall.setResult(new ReturnResultImpl(null == value ? Object.class : value.getClass(), value));
      
      this.result = value;
      this.done = true;
      this.got = true;
      return this.result;
    } catch (Throwable t) {
      this.exception = t;
      throw new ExecutionException(t);
    } finally {
      client.disconnect();
    }
  }

  private RpcCall generateRpcCall(String methodName, Object... args) {
    Request request = new Request();
    request.setAsync(Async.SYNC);
    request.setMethodArgs(args);
    request.setMethodName(methodName);
    request.setRequestId(UUID.randomUUID().toString());
    request.setServiceName("ASYNC");
    request.setVersion("1.0.0");
    RpcCall rpcCall = new RpcCallImpl(request);
    return rpcCall;
  }
}
