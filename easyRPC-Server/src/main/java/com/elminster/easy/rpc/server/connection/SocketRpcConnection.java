package com.elminster.easy.rpc.server.connection;

import java.io.IOException;
import java.net.Socket;

import com.elminster.easy.rpc.protocol.ReqestProtocal;
import com.elminster.easy.rpc.protocol.RequestHeaderProtocal;
import com.elminster.easy.rpc.protocol.ResponseProtocal;
import com.elminster.easy.rpc.protocol.VersionProtocal;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.processor.ReturnResult;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;
import com.elminster.easy.rpc.version.VersionChecker;

public class SocketRpcConnection extends RpcConnectionImpl {

  private final Socket socket;
  private final RpcServiceProcessor processor;
  
  public SocketRpcConnection(RpcServer server, Socket socket) {
    super(server);
    this.socket = socket;
  }

  @Override
  protected void doRun() {
    // TODO Auto-generated method stub
    VersionProtocal versionProtocal = null;
    RequestHeaderProtocal requestHeaderProtocal = null;
    ReqestProtocal requestProtocal = null;
    ResponseProtocal responseProtocal = null;
    try {
      // check version
      versionProtocal.decode();
      String clientVersion = versionProtocal.getVersion();
      if (rpcServer.isVersionCheck()) {
        if (!VersionChecker.compatible(clientVersion, rpcServer.getVersion())) {
          // TODO return exception and disconnection
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    while (!Thread.currentThread().isInterrupted()) {
      try {
        // start serve RPC calls
        requestHeaderProtocal.decode();
        String encodingName = requestHeaderProtocal.getEncoding();
        
        requestProtocal.decode();
        String serviceName = requestProtocal.getServiceName();
        String methodName = requestProtocal.getMethodName();
        Object[] args = requestProtocal.getMethodArgs();
        ReturnResult result = null;
        try {
          result = processor.invokeServiceMethod(serviceName, methodName, args);
        } catch (Throwable e) {
          // TODO Auto-generated catch block
          // wirte exception and continue
          e.printStackTrace();
          continue;
        }
        Class<?> returnType = result.getReturnType();
        Object returnValue = result.getReturnValue();
        responseProtocal.setVoid(returnType == Void.TYPE);
        responseProtocal.setReturnValue(returnValue);
        responseProtocal.encode();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

}
