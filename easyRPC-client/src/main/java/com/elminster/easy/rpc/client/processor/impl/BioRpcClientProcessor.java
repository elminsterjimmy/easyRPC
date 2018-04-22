package com.elminster.easy.rpc.client.processor.impl;

import java.io.EOFException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.call.ReturnResult;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.impl.ReturnResultImpl;
import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.client.async.AsyncFuture;
import com.elminster.easy.rpc.client.connection.Connection;
import com.elminster.easy.rpc.client.context.impl.InvokerContextImpl;
import com.elminster.easy.rpc.client.exception.AsyncCallNoAckException;
import com.elminster.easy.rpc.client.impl.RpcClientFactoryImpl;
import com.elminster.easy.rpc.client.processor.RpcClientProcessor;
import com.elminster.easy.rpc.data.Async;
import com.elminster.easy.rpc.data.Request;
import com.elminster.easy.rpc.data.Response;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.CodecException;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.RequestProtocol;
import com.elminster.easy.rpc.protocol.ResponseProtocol;
import com.elminster.easy.rpc.protocol.impl.RequestProtocolImpl;
import com.elminster.easy.rpc.protocol.impl.ResponseProtocolImpl;

/**
 * The Bio Rpc Client Processor.
 * 
 * @author jinggu
 * @version 1.0
 */
public class BioRpcClientProcessor implements RpcClientProcessor {

  private static final Logger logger = LoggerFactory.getLogger(BioRpcClientProcessor.class);

  private final InvokerContextImpl invokerContext;
  private final RequestProtocol requestProtocol;
  private final ResponseProtocol responseProtocol;
  private final Connection conn;

  public BioRpcClientProcessor(final RpcEncodingFactory encodingFactory, InvokerContextImpl invokerContext, Connection conn) {
    this.requestProtocol = new RequestProtocolImpl(encodingFactory);
    this.responseProtocol = new ResponseProtocolImpl(encodingFactory);
    this.invokerContext = invokerContext;
    this.conn = conn;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object invokeService(RpcCall rpcCall) throws Throwable {
    rpcCall.setContext(invokerContext); // FIXME it is ugly to set the context here
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("Before calling RPC [%s]", rpcCall));
    }
    try {
      
      Request request = new Request();
      request.setRequestId(rpcCall.getRequestId());
      request.setAsync(rpcCall.isAsyncCall() ? Async.ASYNC : Async.SYNC);
      request.setServiceName(rpcCall.getServiceName());
      request.setMethodName(rpcCall.getMethodName());
      request.setMethodArgs(rpcCall.getArgs());
      rpcCall.setRpcCallStartAt(System.currentTimeMillis());
      try {
        requestProtocol.encode(request);
      } catch (CodecException codece) {
        throw codece;
      }
      
      Response response = responseProtocol.decode();
      if (!rpcCall.getRequestId().equals(response.getReqeustId())) {
        logger.warn(String.format("unmatched request id: expect [%s] actual [%s].", rpcCall.getRequestId(), response.getReqeustId()));
      }
      Object rtn;
      Object value = response.getReturnValue();
      if (value instanceof RpcException) {
        throw (RpcException) value;
      }
      if (rpcCall.isAsyncCall()) {
        // expect the async response
        if ("OK".equals(value)) {
          // check if it is a void return
          boolean isVoidReturn = rpcCall.isVoidReturn();
          if (isVoidReturn) {
            // fill rpc call
            rpcCall.setResult(new ReturnResultImpl(Void.class, value));
            return null;  // do nothing since the async call is void
          } else {
            RpcClient client = RpcClientFactoryImpl.INSTANCE.createRpcClient(conn.getRpcClient());
            AsyncFuture futrue = new AsyncFuture(client, rpcCall);
            rtn = futrue;
          }
        } else {
          throw new AsyncCallNoAckException(String.format("No ACK for Async call: [%s].", rpcCall));
        }
      } else {
        boolean isVoid = response.isVoid();
        rtn = value;
        ReturnResult result;
        if (isVoid) {
          result = new ReturnResultImpl(Void.class, value);
        } else {
          result = new ReturnResultImpl(null == value ? Object.class : value.getClass(), value);
        }
        rpcCall.setResult(result);
        rpcCall.setRpcCallEndAt(System.currentTimeMillis());
        if (logger.isDebugEnabled()) {
          logger.debug(String.format("After calling RPC [%s]", rpcCall));
        }
      }
      return rtn;
    } catch (IOException ioe) {
      if (ioe instanceof EOFException) {
        String msg = String.format("Connection with Rpc Server is broken. rpcCall [%s]", rpcCall);
        logger.error(msg);
        throw new RpcException(msg, ioe);
      } else {
        throw ioe;
      }
    }
  }
}
