package com.elminster.easy.rpc.client.async;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.easy.rpc.call.ReturnResult;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.impl.ReturnResultImpl;
import com.elminster.easy.rpc.client.connection.Connection;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.AsyncResponseProtocol;
import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol;
import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol.Frame;
import com.elminster.easy.rpc.protocol.ResponseProtocol;
import com.elminster.easy.rpc.protocol.impl.ProtocolFactoryImpl;

/**
 * The Async Future.
 * 
 * @author jinggu
 * @version 1.0
 */
public class AsyncFuture implements Future<Object> {

  private static final Logger logger = LoggerFactory.getLogger(AsyncFuture.class);

  protected final RpcEncodingFactory encodingFactory;
  protected final RpcCall rpcCall;
  protected final String requestId;
  protected final Connection conn;
  protected AsyncResponseProtocol asyncProtocol;
  protected ResponseProtocol responseProtocol;
  protected ConfirmFrameProtocol confirmFrameProtocol;
  protected Object result;
  protected Throwable exception;
  protected volatile boolean done;
  protected volatile boolean cancelled;

  public AsyncFuture(RpcEncodingFactory encodingFactory, RpcCall rpcCall, Connection conn) {
    this.rpcCall = rpcCall;
    this.requestId = rpcCall.getRequestId();
    this.encodingFactory = encodingFactory;
    this.conn = conn;
    try {
      asyncProtocol = (AsyncResponseProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(AsyncResponseProtocol.class, encodingFactory);
      responseProtocol = (ResponseProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ResponseProtocol.class, encodingFactory);
      confirmFrameProtocol = (ConfirmFrameProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ConfirmFrameProtocol.class, encodingFactory);
    } catch (ObjectInstantiationExcption e) {
      // should not happen.
    }
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    this.cancelled = true;
    if (!this.done) {
      asyncProtocol.setRequestId(requestId);
      asyncProtocol.cancel();

      try {
        asyncProtocol.encode();
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (RpcException e) {
        throw new RuntimeException(e);
      } finally {
        try {
          conn.disconnect();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
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
      asyncProtocol.setRequestId(requestId);
      asyncProtocol.queryIsDone();
      try {
        asyncProtocol.encode();
      } catch (IOException | RpcException e) {
        this.exception = e;
        throw new RuntimeException(e);
      }

      try {
        asyncProtocol.decode();
      } catch (IOException | RpcException e) {
        this.exception = e;
        throw new RuntimeException(e);
      }

      String requestId = asyncProtocol.getRequestId();
      if (!requestId.equals(this.requestId)) {
        // should not happened
        this.exception = new RequestIdNotMatchException(this.requestId, requestId);
        throw new RuntimeException(exception);
      }
      this.done = asyncProtocol.isDone();
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
    if (this.done) {
      return this.result;
    }
    if (this.cancelled) {
      throw new InterruptedException(String.format("Request [%s] is Cancelled!", requestId));
    }
    if (null != this.exception) {
      throw new ExecutionException(this.exception);
    }
    try {
      asyncProtocol.setRequestId(requestId);
      asyncProtocol.setGet();
      asyncProtocol.setTimeout(unit.toMillis(timeout));
      try {
        asyncProtocol.encode();
      } catch (RpcException e) {
        this.exception = e;
        throw new ExecutionException(e);
      }

      try {
        // wait for done by using blocking io
        confirmFrameProtocol.decode();

        if (Frame.FRAME_CANCELLED.getFrame() == confirmFrameProtocol.getFrame()) {
          // cancelled by other thread
          this.cancelled = true;
          throw new InterruptedException(String.format("Request [%s] is Cancelled!", requestId));
        } else if (Frame.FRAME_TIMEOUT.getFrame() == confirmFrameProtocol.getFrame()) {
          throw new TimeoutException(String.format("Request [%s] is timeout!", requestId));
        } else if (Frame.FRAME_RESPONSE.getFrame() == confirmFrameProtocol.getFrame()) {
          Object returnValue = null;
          ReturnResult result;
          Long invokeStart = null;
          Long invokeEnd = null;
          try {
            responseProtocol.decode();
            // String id = responseProtocol.getRequestId();
            boolean isVoid = responseProtocol.isVoid();
            invokeStart = responseProtocol.getInvokeStart();
            invokeEnd = responseProtocol.getInvokeEnd();
            if (!isVoid) {
              returnValue = responseProtocol.getReturnValue();
              result = new ReturnResultImpl(null == returnValue ? Object.class : returnValue.getClass(), returnValue);
            } else {
              result = new ReturnResultImpl(Void.class, returnValue);
            }
          } catch (RpcException e) {
            // decoding error
            returnValue = e;
            result = new ReturnResultImpl(null == returnValue ? Exception.class : returnValue.getClass(), returnValue);
          }
          rpcCall.setResult(result);
          rpcCall.setRpcCallEndAt(System.currentTimeMillis());
          rpcCall.setInvokeStartAt(invokeStart);
          rpcCall.setInvokeEndAt(invokeEnd);
          if (logger.isDebugEnabled()) {
            logger.debug(String.format("After calling RPC [%s]", rpcCall));
          }

          if (returnValue instanceof Throwable) {
            this.exception = (Throwable) returnValue;
          }
          this.result = returnValue;
        } else {
          RpcException rpce = (RpcException) encodingFactory.readObjectNullable();
          throw rpce;
        }
      } catch (RpcException rpce) {
        this.exception = rpce;
      } finally {
        // disconnect

      }
      this.done = true;
    } catch (IOException ioe) {
      this.exception = ioe;
    } finally {
      try {
        conn.disconnect();
      } catch (IOException e) {
        this.exception = e;
      }
    }
    if (null != this.exception) {
      throw new ExecutionException(this.exception);
    }
    return this.result;
  }

}
