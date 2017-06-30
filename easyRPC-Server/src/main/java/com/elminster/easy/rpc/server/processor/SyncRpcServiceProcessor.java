package com.elminster.easy.rpc.server.processor;

import com.elminster.common.util.ArrayUtil;
import com.elminster.common.util.ReflectUtil;
import com.elminster.common.util.TypeUtil;
import com.elminster.common.util.TypeUtil.CompactedType;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.service.RpcService;

public class SyncRpcServiceProcessor implements RpcServiceProcessor {
  
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
      throw new RpcException(String.format("Method [%s] is NOT pubulished in Service [%s]! Context: [%s]", methodName, service, context));
    }
    try {
      final Object rtn = ReflectUtil.invoke(service, methodName, args);
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
    }
  }

}
