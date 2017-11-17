package com.elminster.easy.rpc.client.processor.impl;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.util.BinaryUtil;
import com.elminster.easy.rpc.call.ReturnResult;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.impl.ReturnResultImpl;
import com.elminster.easy.rpc.client.async.AsyncFuture;
import com.elminster.easy.rpc.client.connection.Connection;
import com.elminster.easy.rpc.client.context.impl.InvokerContextImpl;
import com.elminster.easy.rpc.client.impl.RpcClientImpl;
import com.elminster.easy.rpc.client.processor.RpcClientProcessor;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryNotFoundException;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryRepository;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.request.Header;
import com.elminster.easy.rpc.request.Response;
import com.elminster.easy.rpc.request.RpcRequest;
import com.elminster.easy.rpc.request.impl.HeaderImpl;
import com.elminster.easy.rpc.request.impl.RpcRequestImpl;
import com.elminster.easy.rpc.serializer.Serializer;
import com.elminster.easy.rpc.serializer.impl.HeaderSerializer;
import com.elminster.easy.rpc.serializer.impl.ResponseSerializer;
import com.elminster.easy.rpc.serializer.impl.RpcRequestSerializer;

/**
 * The Bio Rpc Client Processor.
 * 
 * @author jinggu
 * @version 1.0
 */
public class BioRpcClientProcessor implements RpcClientProcessor {

  private static final Logger logger = LoggerFactory.getLogger(BioRpcClientProcessor.class);

  private final RpcEncodingFactory encodingFactory;
  private final InvokerContextImpl invokerContext;
  private final Connection conn;
  private final RpcEncodingFactoryRepository repository;

  public BioRpcClientProcessor(RpcEncodingFactory encodingFactory_, InvokerContextImpl invokerContext, Connection conn) {
    this.encodingFactory = encodingFactory_;
    this.invokerContext = invokerContext;
    this.conn = conn;
    repository = new RpcEncodingFactoryRepository() {

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
      ((RpcRequestImpl)rpcCall.getRequest()).setEncoding(encodingFactory.getName());

      rpcCall.setRpcCallStartAt(System.currentTimeMillis());
      Serializer<Header> headerSerializer = new HeaderSerializer(repository, encodingFactory.getCodec());
      HeaderImpl header = new HeaderImpl();
      header.setRequestType(Header.SYNC_REQUEST);
      header.setVersion(RpcClientImpl.getClientVersion());
      byte[] mHeader = headerSerializer.serialize(header);
      
      Serializer<RpcRequest> reqSerializer = new RpcRequestSerializer(repository, encodingFactory.getCodec());
      byte[] reqMsg = reqSerializer.serialize(rpcCall.getRequest());
      encodingFactory.writeObjectNullable(BinaryUtil.concatBytes(mHeader, reqMsg));
      encodingFactory.flush();
      
      if (rpcCall.isAsyncCall()) {
        boolean isVoidReturn = rpcCall.isVoidReturn();
        AsyncFuture future = new AsyncFuture(encodingFactory, rpcCall, conn);
        if (isVoidReturn) {
          // TODO return
        }
        return future;
      } else {
        byte[] resMsg = (byte[]) encodingFactory.readObjectNullable();
        ByteBuffer byteBuffer = ByteBuffer.wrap(resMsg);
        Serializer<Response> resSerializer = new ResponseSerializer(repository, encodingFactory.getCodec());
        Response response = resSerializer.deserialize(byteBuffer);

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
          throw (Throwable) returnValue;
        }
        return returnValue;
      }
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
