package com.elminster.easy.rpc.client.processor.impl;

import java.io.EOFException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.easy.rpc.call.ReturnResult;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.impl.ReturnResultImpl;
import com.elminster.easy.rpc.client.context.impl.InvokerContextImpl;
import com.elminster.easy.rpc.client.processor.RpcClientProcessor;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol;
import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol.Frame;
import com.elminster.easy.rpc.protocol.RequestHeaderProtocol;
import com.elminster.easy.rpc.protocol.RequestProtocol;
import com.elminster.easy.rpc.protocol.ResponseProtocol;
import com.elminster.easy.rpc.protocol.impl.ProtocolFactoryImpl;

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

  public BioRpcClientProcessor(RpcEncodingFactory encodingFactory, InvokerContextImpl invokerContext) {
    this.encodingFactory = encodingFactory;
    this.invokerContext = invokerContext;
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
      ConfirmFrameProtocol confirmFrameProtocol;
      RequestHeaderProtocol requestHeaderProtocol;
      try {
        confirmFrameProtocol = (ConfirmFrameProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ConfirmFrameProtocol.class, encodingFactory);
        requestHeaderProtocol = (RequestHeaderProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(RequestHeaderProtocol.class, encodingFactory);
      } catch (ObjectInstantiationExcption e) {
        encodingFactory.writeIsNotNull(false);
        throw e;
      }
      confirmFrameProtocol.nextFrame(Frame.FRAME_HEADER.getFrame());

      requestHeaderProtocol.setEncoding(encodingFactory.getEncodingName());
      requestHeaderProtocol.encode();

      if (!confirmFrameProtocol.expact(Frame.FRAME_REQUEST.getFrame())) {
        RpcException rpce = (RpcException) encodingFactory.readObjectNullable();
        throw rpce;
      }

      RequestProtocol requestProtocol;
      ResponseProtocol responseProtocol;
      try {
        requestProtocol = (RequestProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(RequestProtocol.class, encodingFactory);
        responseProtocol = (ResponseProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ResponseProtocol.class, encodingFactory);
      } catch (ObjectInstantiationExcption e) {
        String msg = "Cannot instantiate RequestProtocol!";
        logger.error(msg);
        confirmFrameProtocol.nextFrame(Frame.FRAME_FAIL.getFrame());
        throw new RpcException(msg, e);
      }

      rpcCall.setRpcCallStartAt(System.currentTimeMillis());
      confirmFrameProtocol.nextFrame(Frame.FRAME_OK.getFrame());
      requestProtocol.setRequestId(rpcCall.getRequestId());
      requestProtocol.setAsyncCall(rpcCall.isAsyncCall());
      requestProtocol.setServiceName(rpcCall.getServiceName());
      requestProtocol.setMethodName(rpcCall.getMethodName());
      requestProtocol.setMethodArgs(rpcCall.getArgs());
      try {
        requestProtocol.encode();
      } catch (RpcException rpce) {
        // failed encode
        throw rpce;
      }
      
      // expect the response
      if (!confirmFrameProtocol.expact(Frame.FRAME_RESPONSE.getFrame())) {
        RpcException rpce = (RpcException) encodingFactory.readObjectNullable();
        throw rpce;
      }
      
      Object returnValue = null;
      ReturnResult result;
      Long invokeStart = null;
      Long invokeEnd = null;
      try {
        responseProtocol.decode();
//        String id = responseProtocol.getRequestId(); 
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
        throw (Throwable) returnValue;
      }
      return returnValue;

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
