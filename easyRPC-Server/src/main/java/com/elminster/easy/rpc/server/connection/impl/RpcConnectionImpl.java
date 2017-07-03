package com.elminster.easy.rpc.server.connection.impl;

import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.context.InvokeContext;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.listener.RpcProcessEvent;
import com.elminster.easy.rpc.server.listener.RpcServerListener;
import com.elminster.easy.rpc.server.processor.ReturnResult;

abstract public class RpcConnectionImpl implements RpcConnection {
  
  protected final RpcServer rpcServer;
  
  public RpcConnectionImpl(RpcServer rpcServer) {
    this.rpcServer = rpcServer;
  }

  @Override
  public void run() {
    doRun();
  }
  
  abstract protected void doRun();

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    Thread.currentThread().interrupt();
  }

  public RpcServer getRpcServer() {
    return this.rpcServer;
  }
  
  protected enum Messages {
    CLIENT_DISCONNECTED {
      @Override
      public String getMessage() {
        return "Client [%s] Disconnected!";
      }
    },
    CANNOT_GENERATE_RPCEXCPETION {
      @Override
      public String getMessage() {
        return "Cannot Generate RpcException!";
      }
    },
    CANNOT_FOUND_ENCODINGFACTORY {
      @Override
      public String getMessage() {
        return "Cannot Found EncodingFactory named [%s] for Client [%s].";
      }
    },
    CANNOT_DECODE_REQUEST {
      @Override
      public String getMessage() {
        return "Failed to decode the request from Client [%s].";
      }
    },
    CANNOT_ENCODE_RESPONSE {
      @Override
      public String getMessage() {
        return "Failed to encode the response to Client [%s].";
      }
    },
    RPC_REQUEST_INVOKE {
      @Override
      public String getMessage() {
        return "Invoke RPC Call [%s@%s] with args.len [%d] from Client [%s].";
      }
    },
    CANNOT_INS_PROCESSOR {
      @Override
      public String getMessage() {
        return "Cannot Instantiation Service Processor for Service [%s] for Client [%s]. ";
      }
    },
    FAILED_INVOKE_REQUEST {
      @Override
      public String getMessage() {
        return "Failed to Invoke RPC Call [%s@%s] with args.len [%d] from Client [%s].";
      }
    };
    
    abstract public String getMessage();
  }
  
  protected void beforeProcess(String serviceName, String methodName, Object[] args, InvokeContext context) {
    for (RpcServerListener listener : rpcServer.getServerListeners()) {
      listener.preProcess(new RpcProcessEvent(serviceName, methodName, args, context));
    }
  }
  
  protected void afterProcess(String serviceName, String methodName, Object[] args, ReturnResult result, InvokeContext context) {
    for (RpcServerListener listener : rpcServer.getServerListeners()) {
      listener.postProcess(new RpcProcessEvent(serviceName, methodName, args, result, context));
    }
  }
}
