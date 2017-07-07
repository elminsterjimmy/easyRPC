package com.elminster.easy.rpc.client.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.elminster.easy.rpc.codec.RpcEncodingFactory;

public class AsyncFuture implements Future<Object> {
  
  protected RpcEncodingFactory encodingFactory;
  protected Object result;
  protected Throwable exception;
  protected volatile boolean done;
  protected volatile boolean cancelled;
  
  public AsyncFuture() {
    
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    this.cancelled = true;
    synchronized (this) {
      super.notifyAll();
    }
    return true;
}

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public boolean isDone() {
    return this.done;
  }

  @Override
  public Object get() throws InterruptedException, ExecutionException {
    synchronized (this) {
      if (!(this.done)) {
        super.wait();
      }
    }
    if (this.cancelled) {
      throw new InterruptedException("Operation Cancelled");
    }
    if (this.exception != null) {
      throw new ExecutionException(this.exception);
    }
    return this.result;
  }

  @Override
  public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    synchronized (this) {
      if (!(this.done)) {
        unit.timedWait(this, timeout);
      }
    }
    if (this.cancelled) {
      throw new InterruptedException("Operation Cancelled");
    }
    if (!(this.done)) {
      throw new TimeoutException("Timeout Exceeded");
    }
    if (this.exception != null) {
      throw new ExecutionException(this.exception);
    }
    return this.result;
  }

}
