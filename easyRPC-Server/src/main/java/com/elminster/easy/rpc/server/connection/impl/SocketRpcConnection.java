package com.elminster.easy.rpc.server.connection.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.CoreCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.exception.ObjectInstantiationExcption;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.exception.VersionCompatibleException;
import com.elminster.easy.rpc.protocol.RequestHeaderProtocol;
import com.elminster.easy.rpc.protocol.RequestProtocol;
import com.elminster.easy.rpc.protocol.ResponseProtocol;
import com.elminster.easy.rpc.protocol.ShakehandProtocol;
import com.elminster.easy.rpc.protocol.VersionProtocol;
import com.elminster.easy.rpc.protocol.impl.ProtocolFactoryImpl;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.processor.InvokeContextImpl;
import com.elminster.easy.rpc.server.processor.InvokeContextImpl.InvokeContextImplBuilder;
import com.elminster.easy.rpc.server.processor.ReturnResult;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessorFactoryImpl;
import com.elminster.easy.rpc.version.VersionChecker;

public class SocketRpcConnection extends RpcConnectionImpl {

  private static final Logger logger = LoggerFactory.getLogger(SocketRpcConnection.class);

  private final Socket socket;
  private final RpcServiceProcessor processor;

  private final InetAddress localAddr;
  private final InetAddress remoteAddr;
  private final int localPort;
  private final int remotePort;

  public SocketRpcConnection(RpcServer server, Socket socket) {
    super(server);
    this.socket = socket;
    localAddr = socket.getLocalAddress();
    remoteAddr = socket.getInetAddress();
    localPort = socket.getLocalPort();
    remotePort = socket.getPort();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doRun() {
    // init the core codec
    try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
      CoreCodec util = CoreCodecFactory.INSTANCE.getCoreCodec(in, out);

      InvokeContextImplBuilder builder = new InvokeContextImplBuilder();
      InvokeContextImpl invokeContext = builder.withServerHost(localAddr)
          .withClientHost(remoteAddr)
          .withClientPort(remotePort)
          .withServerPort(localPort).build();
      RpcEncodingFactory defaultEncodingFactory = rpcServer.getEncodingFactory("default", util);
      ShakehandProtocol shakehandProtocol;
      VersionProtocol versionProtocol;
      RequestHeaderProtocol requestHeaderProtocol;
      try {
        shakehandProtocol = (ShakehandProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ShakehandProtocol.class, defaultEncodingFactory);
        versionProtocol = (VersionProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(VersionProtocol.class, defaultEncodingFactory);
        requestHeaderProtocol = (RequestHeaderProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(RequestHeaderProtocol.class, defaultEncodingFactory);
      } catch (ObjectInstantiationExcption e1) {
        logger.error("Cannot instantiate base protocols, and this should NOT happened!");
        defaultEncodingFactory.writeIsNotNull(false);
        return;
      }
      try {
        // shakehand
        shakehandProtocol.decode();
        shakehandProtocol.complete();
      } catch (IOException ioe) {
        logger.error(String.format(Messages.CLIENT_DISCONNECTED.getMessage(), remoteAddr, remotePort));
        return;
      } catch (RpcException rpce) {
        logger.error(rpce.getMessage(), rpce);
        return;
      }

      try {
        // check version
        versionProtocol.decode();
        String clientVersion = versionProtocol.getVersion();
        String serverVersion = rpcServer.getVersion();
        invokeContext.setClientVersion(clientVersion);
        invokeContext.setServerVersion(serverVersion);
        if (rpcServer.isVersionCheck()) {
          if (!VersionChecker.compatible(clientVersion, serverVersion)) {
            // return exception and disconnection
            String msg = String.format("Incompatible versions! Server version is [%s] but Client version is [%s].", serverVersion, clientVersion);
            throw new VersionCompatibleException(msg);
          }
        }
        versionProtocol.complete();
      } catch (IOException e) {
        logger.error(String.format(Messages.CLIENT_DISCONNECTED.getMessage(), invokeContext));
        return;
      } catch (RpcException rpce) {
        versionProtocol.fail();
        writeRpcException(defaultEncodingFactory, rpce);
        return;
      }

      while (!Thread.currentThread().isInterrupted()) {
        try {
          // start serve RPC calls
          RequestProtocol requestProtocol = null;
          ResponseProtocol responseProtocol = null;
          
          try {
            requestHeaderProtocol.decode();
          } catch (RpcException rpce) {
            requestHeaderProtocol.fail();
            String message = String.format(Messages.CANNOT_DECODE_REQUEST.getMessage(), invokeContext);
            writeException(defaultEncodingFactory, rpce, message);
            continue;
          }
          
          
          String encodingName = requestHeaderProtocol.getEncoding();

          RpcEncodingFactory rpcEncodingFactory = rpcServer.getEncodingFactory(encodingName, util);
          if (null == rpcEncodingFactory) {
            requestHeaderProtocol.fail();
            String message = String.format(Messages.CANNOT_FOUND_ENCODINGFACTORY.getMessage(), encodingName, invokeContext);
            RpcException rpcException = new RpcException(message);
            writeRpcException(defaultEncodingFactory, rpcException);
            continue; // start over
          }

          try {
            requestProtocol = (RequestProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(RequestProtocol.class, rpcEncodingFactory);
            requestHeaderProtocol.complete();
          } catch (ObjectInstantiationExcption e) {
            // unexpected error
            requestHeaderProtocol.fail();
            String message = "Cannot instantiate request protocols, and this should NOT happened!";
            writeException(defaultEncodingFactory, e, message);
            continue; // start over
          }

          try {
            requestProtocol.decode();
          } catch (RpcException rpce) {
            requestProtocol.fail();
            String message = String.format(Messages.CANNOT_DECODE_REQUEST.getMessage(), invokeContext);
            writeException(defaultEncodingFactory, rpce, message);
            continue;
          }
          String serviceName = requestProtocol.getServiceName();
          String methodName = requestProtocol.getMethodName();
          Object[] args = requestProtocol.getMethodArgs();

          if (logger.isDebugEnabled()) {
            logger.debug(String.format(Messages.RPC_REQUEST_INVOKE.getMessage(), methodName, serviceName, args.length, invokeContext));
          }

          try {
            RpcServiceProcessorFactoryImpl.INSTANCE.createServiceProcessor(rpcServer);
          } catch (ObjectInstantiationExcption e) {
            logger.error(String.format(Messages.CANNOT_INS_PROCESSOR.getMessage(), serviceName, invokeContext));
          }
          
          ReturnResult result = null;
          try {
            result = processor.invokeServiceMethod(invokeContext, serviceName, methodName, args);
          } catch (Throwable e) {
            requestProtocol.fail();
            if (e instanceof RpcException) {
              writeRpcException(defaultEncodingFactory, (RpcException)e);
            } else {
              String message = String.format(Messages.CANNOT_DECODE_REQUEST.getMessage(), methodName, serviceName, args.length, invokeContext);
              writeException(defaultEncodingFactory, e, message);
            }
            continue; // start over
          }
          
          try {
            responseProtocol = (ResponseProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ResponseProtocol.class, rpcEncodingFactory);
            requestProtocol.complete();
          } catch (ObjectInstantiationExcption e) {
            // unexpected error
            requestHeaderProtocol.fail();
            String message = "Cannot instantiate request protocols, and this should NOT happened!";
            writeException(defaultEncodingFactory, e, message);
            continue; // start over
          }
          
          Class<?> returnType = result.getReturnType();
          Object returnValue = result.getReturnValue();
          responseProtocol.setVoid(returnType == Void.class || returnType == void.class);
          responseProtocol.setReturnValue(returnValue);
          try {
            responseProtocol.encode();
            responseProtocol.complete();
          } catch (RpcException e) {
            responseProtocol.fail();
            String message = String.format(Messages.CANNOT_ENCODE_RESPONSE.getMessage(), invokeContext);
            writeException(defaultEncodingFactory, e, message);
            continue; // start over
          }
        } catch (IOException ioe) {
          logger.error(String.format(Messages.CLIENT_DISCONNECTED.getMessage(), remoteAddr, remotePort));
        }
      }
    } catch (IOException e) {

    }
  }

  private void writeException(RpcEncodingFactory encodingFactory, Throwable e, String message) throws IOException {
    RpcException rpce = new RpcException(message, e);
    writeRpcException(encodingFactory, rpce);
  }
  
  private void writeRpcException(RpcEncodingFactory encodingFactory, RpcException e) throws IOException {
    logger.error(e.getMessage(), e);
    try {
      encodingFactory.writeObjectNullable(e);
    } catch (RpcException e1) {
      logger.error(Messages.CANNOT_GENERATE_RPCEXCPETION.getMessage());
      encodingFactory.writeIsNotNull(false);
    }
  }

  @Override
  public InetAddress getRemoteAddress() {
    return remoteAddr;
  }

  @Override
  public int getRemotePort() {
    return remotePort;
  }

  @Override
  public InetAddress getLocalAddress() {
    return localAddr;
  }

  @Override
  public int getLocalPort() {
    return localPort;
  }
  
  @Override
  public String toString() {
    return String.format("SocketRpcConnection [ Server=[ host:%s, port:%d ] | Client=[host:%s, port:%d] ]", localAddr, localPort, remoteAddr, remotePort);
  }
}
