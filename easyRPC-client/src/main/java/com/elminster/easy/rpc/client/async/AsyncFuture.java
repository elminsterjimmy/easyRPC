package com.elminster.easy.rpc.client.async;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.util.BinaryUtil;
import com.elminster.easy.rpc.call.ReturnResult;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.impl.ReturnResultImpl;
import com.elminster.easy.rpc.client.connection.Connection;
import com.elminster.easy.rpc.client.impl.RpcClientImpl;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryNotFoundException;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryRepository;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.AsyncRequestProtocol;
import com.elminster.easy.rpc.request.AsyncQueryResponse;
import com.elminster.easy.rpc.request.Header;
import com.elminster.easy.rpc.request.Response;
import com.elminster.easy.rpc.request.impl.AsyncQueryRequestImpl;
import com.elminster.easy.rpc.request.impl.HeaderImpl;
import com.elminster.easy.rpc.serializer.impl.AsyncQueryRequestSerializer;
import com.elminster.easy.rpc.serializer.impl.AsyncQueryResponseSerializer;
import com.elminster.easy.rpc.serializer.impl.HeaderSerializer;
import com.elminster.easy.rpc.serializer.impl.ResponseSerializer;

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
  protected final String id2Request;
  protected final Connection conn;
  protected Object result;
  protected Throwable exception;
  protected volatile boolean done = false;
  protected volatile boolean cancelled = false;
  
  
  HeaderSerializer headerSerializer;
  AsyncQueryRequestSerializer areqSerializer;
  AsyncQueryResponseSerializer aresSerializer;
  ResponseSerializer resSerializer;

  public AsyncFuture(RpcEncodingFactory encodingFactory_, RpcCall rpcCall, Connection conn) {
    this.rpcCall = rpcCall;
    this.id2Request = rpcCall.getRequestId();
    this.encodingFactory = encodingFactory_;
    this.conn = conn;
    
    RpcEncodingFactoryRepository repository = new RpcEncodingFactoryRepository() {

      @Override
      public RpcEncodingFactory getRpcEncodingFactory(String encodingFactoryName, Codec codec) throws RpcEncodingFactoryNotFoundException {
        return encodingFactory;
      }

      @Override
      public void addRpcEncodingFactory(RpcEncodingFactory encodingFactory) {
      }

      @Override
      public void removeRpcEncodingFactory(String encodingFactoryName) {
      }
    };
    
    headerSerializer = new HeaderSerializer(repository, encodingFactory.getCodec());
    areqSerializer = new AsyncQueryRequestSerializer(repository, encodingFactory.getCodec());
    aresSerializer = new AsyncQueryResponseSerializer(repository, encodingFactory.getCodec());
    resSerializer = new ResponseSerializer(repository, encodingFactory.getCodec());
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    this.cancelled = true;
    if (!this.done) {
      try {
        queryAsyncRequest(AsyncRequestProtocol.CANCEL);
        
        byte[] message = (byte[]) encodingFactory.readObjectNullable();
        ByteBuffer byteBuffer = ByteBuffer.wrap(message);
        AsyncQueryResponse response = aresSerializer.deserialize(byteBuffer);
        
        if (!response.getId2Request().equals(this.id2Request)) {
          // should not happened
          this.exception = new RequestIdNotMatchException(this.id2Request, response.getId2Request());
          throw new RuntimeException(exception);
        }
        this.cancelled = response.isCancelled();
      } catch (IOException e) {
        this.exception = e;
        throw new RuntimeException(e);
      } catch (RpcException e) {
        this.exception = e;
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
        queryAsyncRequest(AsyncRequestProtocol.QUERY_DONE);
        
        byte[] message = (byte[]) encodingFactory.readObjectNullable();
        ByteBuffer byteBuffer = ByteBuffer.wrap(message);
        AsyncQueryResponse response = aresSerializer.deserialize(byteBuffer);
        
        if (!response.getId2Request().equals(this.id2Request)) {
          // should not happened
          this.exception = new RequestIdNotMatchException(this.id2Request, response.getId2Request());
          throw new RuntimeException(exception);
        }
        this.done = response.isDone();
      } catch (IOException ioe) {
        this.exception = ioe;
      } catch (RpcException rpce) {
        this.exception = rpce;
      }
    }
    return this.done;
  }
  
  private void queryAsyncRequest(byte action) throws IOException, RpcException {
    queryAsyncRequest(action, 0);
  }

  private void queryAsyncRequest(byte action, long timeout) throws IOException, RpcException {
    HeaderImpl header = new HeaderImpl();
    header.setRequestType(Header.ASYNC_REQUEST);
    header.setVersion(RpcClientImpl.getClientVersion());
    byte[] mHeader = headerSerializer.serialize(header);
    
    AsyncQueryRequestImpl request = new AsyncQueryRequestImpl();
    request.setRequestId(generateRequestId());
    request.setId2Request(id2Request);
    request.setAction(action);
    request.setTimeout(timeout);
    byte[] mAsReq = areqSerializer.serialize(request);
    encodingFactory.writeObjectNullable( BinaryUtil.concatBytes(mHeader, mAsReq));
    encodingFactory.flush();
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
      throw new InterruptedException(String.format("Request [%s] is Cancelled!", id2Request));
    }
    if (null != this.exception) {
      throw new ExecutionException(this.exception);
    }
    try {
      AsyncQueryRequestImpl request = new AsyncQueryRequestImpl();
      request.setRequestId(generateRequestId());
      request.setId2Request(id2Request);
      request.setAction(AsyncRequestProtocol.GET);
      request.setTimeout(timeout);
      queryAsyncRequest(AsyncRequestProtocol.GET, timeout);
      
      byte[] message = (byte[]) encodingFactory.readObjectNullable();
      ByteBuffer byteBuffer = ByteBuffer.wrap(message);
      Response response = resSerializer.deserialize(byteBuffer);
      
      if (response.isCancelled()) {
        // cancelled by other thread
        this.cancelled = true;
        throw new InterruptedException(String.format("Request [%s] is Cancelled!", id2Request));
      } else if (response.isTimedout()) {
        // timed out
        throw new TimeoutException(String.format("Request [%s] is timed out!", id2Request));
      } else {
        Object returnValue = null;
        ReturnResult result;
        boolean isVoid = response.isVoidCall();
        if (!isVoid) {
          returnValue = response.getReturnValue();
          result = new ReturnResultImpl(null == returnValue ? Object.class : returnValue.getClass(), returnValue);
        } else {
          result = new ReturnResultImpl(Void.class, returnValue);
        }
        
        rpcCall.setResult(result);
        rpcCall.setRpcCallEndAt(System.currentTimeMillis());
        if (logger.isDebugEnabled()) {
          logger.debug(String.format("After calling RPC [%s]", rpcCall));
        }

        if (returnValue instanceof Throwable) {
          this.exception = (Throwable) returnValue;
        }
        this.result = returnValue;
      }
      this.done = true;
    } catch (IOException ioe) {
      this.exception = ioe;
    } catch (RpcException rpce) {
      this.exception = rpce;
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
  
  private static String generateRequestId() {
    return UUID.randomUUID().toString();
  }
}
