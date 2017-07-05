package com.elminster.easy.rpc.server.processor.impl;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.util.ArrayUtil;
import com.elminster.common.util.ReflectUtil;
import com.elminster.common.util.TypeUtil;
import com.elminster.common.util.TypeUtil.CompactedType;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.processor.InvokeeContext;
import com.elminster.easy.rpc.server.processor.ReturnResult;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;
import com.elminster.easy.rpc.service.RpcService;

public class SyncRpcServiceProcessor implements RpcServiceProcessor {
  
  private static final Logger logger = LoggerFactory.getLogger(SyncRpcServiceProcessor.class);
  
  private final RpcServer rpcServer;
  
  public SyncRpcServiceProcessor(RpcServer rpcServer) {
    this.rpcServer = rpcServer;
  }

  @Override
  public ReturnResult invokeServiceMethod(InvokeeContext context, String serviceName, String methodName, Object[] args) throws Throwable {
    RpcService service = rpcServer.getService(serviceName);
    if (null == service) {
      throw new RpcException(String.format("Service [%s] is NOT found! Context: [%s].", serviceName, context));
    }
    if (!ArrayUtil.contains(service.getServiceMethods(), methodName)) {
      throw new RpcException(String.format("Method [%s] is NOT pubulished in Service [%s]! Context: [%s]", methodName, serviceName, context));
    }
    long startTs = System.currentTimeMillis();
    try {
      if (logger.isDebugEnabled()) {
        String methodArgs = generMethodArgs(args);
        logger.debug(String.format("Before calling RPC [%s@%s] with args %s on context [%s]", methodName, serviceName, methodArgs, context));
      }
      final Object rtn = ReflectUtil.invoke(service, methodName, args);
      if (logger.isDebugEnabled()) {
        String methodArgs = generMethodArgs(args);
        logger.debug(String.format("After calling RPC [%s@%s] with args %s on context [%s] returns [%s] within [%d] ms.", methodName, serviceName, methodArgs, context, rtn, System.currentTimeMillis() - startTs));
      }
      final CompactedType ct = TypeUtil.getMethodReturnTypeClass(service.getClass(), ReflectUtil.getDeclaredMethod(service.getClass(), methodName, args));
      return new ReturnResult() {
        
        @Override
        public Object getReturnValue() {
          return rtn;
        }
        
        @Override
        public Class<?> getReturnType() {
          return ct.getType();
        }
      };
    } catch (NoSuchMethodException e) {
      throw new RpcException(String.format("Method [%s] is NOT found in Service [%s]! Context: [%s]", methodName, service, context), e);
    } catch (IllegalArgumentException e) {
      throw new RpcException(String.format("Method [%s]'s arguments are illegal in Service [%s]! Context: [%s]", methodName, service, context), e);
    } catch (InvocationTargetException e) {
      if (logger.isDebugEnabled()) {
        String methodArgs = generMethodArgs(args);
        logger.debug(String.format("Exception on calling RPC [%s@%s] with args %s on context [%s] returns [%s] within [%d] ms.", methodName, serviceName, methodArgs, context, e.getTargetException().getMessage(), System.currentTimeMillis() - startTs));
      }
      throw e;
    }
  }
  
  private static String generMethodArgs(Object[] args) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    boolean first = true;
    if (null != args) {
      for (Object arg : args) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append(arg);
      }
    }
    sb.append("]");
    return sb.toString();
  }

}
