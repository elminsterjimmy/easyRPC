package com.elminster.easy.rpc.client.processor.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.easy.rpc.client.context.InvokerContext;
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
  private final InvokerContext invokerContext;

  public BioRpcClientProcessor(RpcEncodingFactory encodingFactory, InvokerContext invokerContext) {
    this.encodingFactory = encodingFactory;
    this.invokerContext = invokerContext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object invokeService(String serviceName, String methodName, Object[] args) throws Throwable {
    if (logger.isDebugEnabled()) {
      String methodArgs = generMethodArgs(args);
      logger.debug(String.format("Calling RPC [%s@%s] with args %s on context [%s]", methodName, serviceName, methodArgs, invokerContext));
    }
    try {
      ConfirmFrameProtocol confirmFrameProtocol = (ConfirmFrameProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ConfirmFrameProtocol.class, encodingFactory);
      
      if (!confirmFrameProtocol.expact(Frame.FRAME_HEADER.getFrame())) {
        RpcException rpce = (RpcException) encodingFactory.readObjectNullable();
        throw rpce;
      }
      
      try {
        RequestHeaderProtocol requestHeaderProtocol = (RequestHeaderProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(RequestHeaderProtocol.class, encodingFactory);
        requestHeaderProtocol.setEncoding(encodingFactory.getEncodingName());
        requestHeaderProtocol.encode();

        if (!confirmFrameProtocol.expact(Frame.FRAME_REQUEST.getFrame())) {
          RpcException rpce = (RpcException) encodingFactory.readObjectNullable();
          throw rpce;
        }

      } catch (ObjectInstantiationExcption e) {
        String msg = "Cannot instantiate RequestHeaderProtocol!";
        logger.error(msg);
        throw new RpcException(msg, e);
      }

      try {
        RequestProtocol requestProtocol = (RequestProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(RequestProtocol.class, encodingFactory);
        requestProtocol.setServiceName(serviceName);
        requestProtocol.setMethodName(methodName);
        requestProtocol.setMethodArgs(args);
        requestProtocol.encode();

        if (!confirmFrameProtocol.expact(Frame.FRAME_RESPONSE.getFrame())) {
          RpcException rpce = (RpcException) encodingFactory.readObjectNullable();
          throw rpce;
        }
      } catch (ObjectInstantiationExcption e) {
        String msg = "Cannot instantiate RequestProtocol!";
        logger.error(msg);
        throw new RpcException(msg, e);
      }

      try {
        ResponseProtocol responseProtocol = (ResponseProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ResponseProtocol.class, encodingFactory);
        responseProtocol.decode();
        Object returnValue = null;
        if (!responseProtocol.isVoid()) {
          returnValue = responseProtocol.getReturnValue();
        }

        if (logger.isDebugEnabled()) {
          String methodArgs = generMethodArgs(args);
          logger.debug(String.format("Calling RPC [%s@%s] with args %s on context [%s] returns [%s]", methodName, serviceName, methodArgs, invokerContext, returnValue));
        }

        if (returnValue instanceof Throwable) {
          throw (Throwable) returnValue;
        }

        return returnValue;
      } catch (ObjectInstantiationExcption e) {
        String msg = "Cannot instantiate ResponseProtocol!";
        logger.error(msg);
        throw new RpcException(msg, e);
      }

    } catch (IOException ioe) {
      String msg = String.format("Connection with Rpc Server is broken. Context [%s]", invokerContext);
      logger.error(msg);
      throw new RpcException(msg, ioe);
    }
  }

  private static String generMethodArgs(Object[] args) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    boolean first = true;
    for (Object arg : args) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }
      sb.append(arg);
    }
    sb.append("]");
    return sb.toString();
  }

}
