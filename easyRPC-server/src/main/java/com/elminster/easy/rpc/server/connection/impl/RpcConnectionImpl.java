package com.elminster.easy.rpc.server.connection.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.thread.IJobMonitor;
import com.elminster.common.thread.Job;
import com.elminster.easy.rpc.call.ReturnResult;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.Status;
import com.elminster.easy.rpc.call.impl.RpcCallImpl;
import com.elminster.easy.rpc.codec.CoreCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.exception.VersionCompatibleException;
import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol;
import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol.Frame;
import com.elminster.easy.rpc.protocol.RequestHeaderProtocol;
import com.elminster.easy.rpc.protocol.RequestProtocol;
import com.elminster.easy.rpc.protocol.ResponseProtocol;
import com.elminster.easy.rpc.protocol.ShakehandProtocol;
import com.elminster.easy.rpc.protocol.VersionProtocol;
import com.elminster.easy.rpc.protocol.impl.ProtocolFactoryImpl;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.context.impl.InvokeeContextImpl;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;
import com.elminster.easy.rpc.version.VersionChecker;

/**
 * The RPC connection base.
 * 
 * @author jinggu
 * @version 1.0
 */
abstract public class RpcConnectionImpl extends Job implements RpcConnection {

  private static final Logger logger = LoggerFactory.getLogger(RpcConnectionImpl.class);

  protected final RpcServer rpcServer;
  protected final Container container;

  protected ConfirmFrameProtocol confirmFrameProtocol;
  protected RequestHeaderProtocol requestHeaderProtocol;
  protected ShakehandProtocol shakehandProtocol;
  protected VersionProtocol versionProtocol;

  // cache the encoding factory since they're heavy objects.
  private Map<String, RpcEncodingFactory> encodingFactoryCache = new ConcurrentHashMap<>();

  public RpcConnectionImpl(RpcServer rpcServer, Container container, long id, String name) {
    super(id, name);
    this.rpcServer = rpcServer;
    this.container = container;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected JobStatus doWork(IJobMonitor monitor) throws Throwable {
    try {
      doRun();
      return monitor.done();
    } finally {
      // remove
      container.removeOpenConnection(this);
    }
  }

  /**
   * Actual work of the connection.
   * 
   * @throws Exception
   *           on error
   */
  abstract protected void doRun() throws Exception;

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
        return "Client [%s] Disconnected Unexpected!";
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
        return "Cannot Instantiation Service Processor for Client [%s]. ";
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

  /**
   * Initialize base protocols.
   * 
   * @param encodingFactory
   *          the encoding factory
   * @throws IOException
   *           on error
   */
  protected void initializeBaseProtocols(RpcEncodingFactory encodingFactory) throws IOException {
    try {
      confirmFrameProtocol = (ConfirmFrameProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ConfirmFrameProtocol.class, encodingFactory);
      requestHeaderProtocol = (RequestHeaderProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(RequestHeaderProtocol.class, encodingFactory);
    } catch (ObjectInstantiationExcption e1) {
      logger.error("Cannot instantiate base protocols, and this should NOT happened!");
      encodingFactory.writeIsNotNull(false);
      throw new IOException("Cannot instantiate base protocols, and this should NOT happened!", e1);
    }
  }

  /**
   * Shake hand with client.
   * 
   * @param encodingFactory
   *          the encoding factory
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on shakehand failed
   */
  protected void shakehand(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    try {
      shakehandProtocol = (ShakehandProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ShakehandProtocol.class, encodingFactory);
    } catch (ObjectInstantiationExcption e) {
      throw new RpcException("Cannot Instantiate ShakehandProtocol.", e);
    }
    shakehandProtocol.decode();
    confirmFrameProtocol.nextFrame(Frame.FRAME_VERSION.getFrame());
  }

  /**
   * Check the version between server and client.
   * 
   * @param encodingFactory
   *          the encoding factory
   * @param invokeContext
   *          the invokee context
   * @throws IOException
   *           on error
   * @throws RpcException
   */
  protected void checkVersion(RpcEncodingFactory encodingFactory, InvokeeContextImpl invokeContext) throws IOException, RpcException {
    try {
      versionProtocol = (VersionProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(VersionProtocol.class, encodingFactory);
    } catch (ObjectInstantiationExcption e) {
      throw new RpcException("Cannot Instantiate VersionProtocol.", e);
    }
    try {
      versionProtocol.decode();
      String clientVersion = versionProtocol.getVersion();
      String serverVersion = rpcServer.getVersion();
      invokeContext.setInvokerVersion(clientVersion);
      invokeContext.setInvokeeVersion(serverVersion);

      // send server version
      versionProtocol.setVersion(serverVersion);
      versionProtocol.encode();
      if (!VersionChecker.compatible(clientVersion, serverVersion)) {
        if (rpcServer.isVersionCheck()) {
          // return exception and disconnection
          confirmFrameProtocol.nextFrame(Frame.FRAME_FAIL.getFrame());
          String msg = String.format("Incompatible versions! Server version is [%s] but Client version is [%s].", serverVersion, clientVersion);
          throw new VersionCompatibleException(msg);
        }
      }
    } catch (RpcException rpce) {
      confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
      writeRpcException(encodingFactory, rpce);
      return;
    }
  }

  /**
   * Invoke a method call.
   * 
   * @param defaultEncodingFactory
   *          the default encoding factory
   * @param invokeContext
   *          the invokee context
   * @param coreCodec
   *          the core codec
   * @throws IOException
   *           on error
   */
  protected void methodCall(RpcEncodingFactory defaultEncodingFactory, InvokeeContextImpl invokeContext, CoreCodec coreCodec) throws IOException {
    // start serve RPC calls
    if (!confirmFrameProtocol.expact(Frame.FRAME_HEADER.getFrame())) {
      throw new IOException();
    }
    try {
      requestHeaderProtocol.decode();
    } catch (RpcException rpce) {
      confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
      String message = String.format(Messages.CANNOT_DECODE_REQUEST.getMessage(), invokeContext);
      writeException(defaultEncodingFactory, rpce, message);
      return;
    }

    String encodingName = requestHeaderProtocol.getEncoding();

    RpcEncodingFactory rpcEncodingFactory = getEncodingFactory(encodingName, coreCodec);
    if (null == rpcEncodingFactory) {
      confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
      String message = String.format(Messages.CANNOT_FOUND_ENCODINGFACTORY.getMessage(), encodingName, invokeContext);
      RpcException rpcException = new RpcException(message);
      writeRpcException(defaultEncodingFactory, rpcException);
      return; // start over
    }

    RequestProtocol requestProtocol = null;
    ResponseProtocol responseProtocol = null;
    try {
      requestProtocol = (RequestProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(RequestProtocol.class, rpcEncodingFactory);
      confirmFrameProtocol.nextFrame(Frame.FRAME_REQUEST.getFrame());
    } catch (ObjectInstantiationExcption e) {
      // unexpected error
      confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
      String message = "Cannot instantiate request protocols, and this should NOT happened!";
      writeException(defaultEncodingFactory, e, message);
      return; // start over
    }

    if (!confirmFrameProtocol.expact(Frame.FRAME_OK.getFrame())) {
      // unexpect frame
      return;
    }
    try {
      requestProtocol.decode();
    } catch (RpcException rpce) {
      confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
      String message = String.format(Messages.CANNOT_DECODE_REQUEST.getMessage(), invokeContext);
      writeException(defaultEncodingFactory, rpce, message);
      return;
    }
    String requestId = requestProtocol.getRequestId();
    boolean isAsyncCall = requestProtocol.isAsyncCall();
    String serviceName = requestProtocol.getServiceName();
    String methodName = requestProtocol.getMethodName();
    Object[] args = requestProtocol.getMethodArgs();

    RpcCall call = new RpcCallImpl(requestId, isAsyncCall, serviceName, methodName, args, invokeContext);

    try {
      responseProtocol = (ResponseProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(ResponseProtocol.class, rpcEncodingFactory);
    } catch (ObjectInstantiationExcption e) {
      // unexpected error
      confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
      String message = "Cannot instantiate request protocols, and this should NOT happened!";
      writeException(defaultEncodingFactory, e, message);
      return; // start over
    }

    RpcServiceProcessor proccessor = container.getServiceProcessor();
    try {
      call.setStatus(Status.UNPROCCESSED);
      if (isAsyncCall) {
        confirmFrameProtocol.nextFrame(Frame.FRAME_ASYNC_RESPONSE.getFrame());
        handleAsyncRpcCall(proccessor, call, defaultEncodingFactory, responseProtocol);
      } else {
        handleSyncRpcCall(proccessor, call, defaultEncodingFactory, responseProtocol);
      }
    } catch (RpcException rpce) {
      confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
      writeRpcException(defaultEncodingFactory, rpce);
      return;
    }
  }

  /**
   * Handle the sync RPC call.
   * 
   * @param proccessor
   *          the processor
   * @param call
   *          the RPC call
   * @param defaultEncodingFactory
   *          the default encoding factory
   * @param responseProtocol
   *          the response protocol
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on error
   */
  abstract protected void handleSyncRpcCall(RpcServiceProcessor proccessor, RpcCall call, RpcEncodingFactory defaultEncodingFactory, ResponseProtocol responseProtocol)
      throws IOException, RpcException;

  /**
   * Handle the async RPC call.
   * 
   * @param proccessor
   *          the processor
   * @param call
   *          the RPC call
   * @param defaultEncodingFactory
   *          the default encoding factory
   * @param responseProtocol
   *          the response protocol
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on error
   */
  abstract protected void handleAsyncRpcCall(RpcServiceProcessor proccessor, RpcCall call, RpcEncodingFactory defaultEncodingFactory, ResponseProtocol responseProtocol)
      throws IOException, RpcException;

  /**
   * Check the RPC call is done or not.
   * 
   * @param proccessor
   *          the processor
   * @param requestId
   *          the RPC call request id
   * @return the RPC call is done or not
   */
  protected boolean checkRpcCallIsDone(RpcServiceProcessor proccessor, String requestId) {
    RpcCall call = proccessor.getRpcCall(requestId);
    return Status.isDone(call.getStatus());
  }

  /**
   * Cancel the RPC call.
   * 
   * @param proccessor
   *          the processor
   * @param requestId
   *          the RPC call request id
   * @return the RPC call is cancelled or not
   */
  protected boolean cancelAsyncRpcCall(RpcServiceProcessor proccessor, String requestId) {
    RpcCall call = proccessor.getRpcCall(requestId);
    return proccessor.cancelRpcCall(call);
  }

  /**
   * Write the RPC call result.
   * 
   * @param rpcCall
   *          the RPC call
   * @param encodingFactory
   *          the encoding factory
   * @param responseProtocol
   *          the response protocol
   * @throws IOException
   *           on error
   */
  protected void writeResult(RpcCall rpcCall, RpcEncodingFactory encodingFactory, ResponseProtocol responseProtocol) throws IOException {
    ReturnResult result = rpcCall.getResult();
    Class<?> returnType = result.getReturnType();
    Object returnValue = result.getReturnValue();
    if (RpcException.class == returnType) {
      confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
      // exception
      RpcException rpce = (RpcException) returnValue;
      writeRpcException(encodingFactory, rpce);
      return;
    } else {
      confirmFrameProtocol.nextFrame(Frame.FRAME_RESPONSE.getFrame());
      responseProtocol.setRequestId(rpcCall.getRequestId());
      responseProtocol.setVoid(returnType == Void.class || returnType == void.class);
      responseProtocol.setReturnValue(returnValue);
      responseProtocol.setInvokeStart(rpcCall.getInvokeStartAt());
      responseProtocol.setInvokeEnd(rpcCall.getInvokeEndAt());
      try {
        responseProtocol.encode();
      } catch (RpcException e) {
        confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
        String message = String.format(Messages.CANNOT_ENCODE_RESPONSE.getMessage(), rpcCall.getContext());
        writeException(encodingFactory, e, message);
        return; // start over
      }
    }
  }

  /**
   * Write an exception.
   * 
   * @param encodingFactory
   *          the encoding factory
   * @param e
   *          the exception
   * @param message
   *          the message
   * @throws IOException
   *           on error
   */
  protected void writeException(RpcEncodingFactory encodingFactory, Throwable e, String message) throws IOException {
    RpcException rpce = new RpcException(message, e);
    writeRpcException(encodingFactory, rpce);
  }

  /**
   * Write a RpcException.
   * 
   * @param encodingFactory
   *          the encoding factory
   * @param e
   *          the RpcException
   * @throws IOException
   *           on error
   */
  protected void writeRpcException(RpcEncodingFactory encodingFactory, RpcException e) throws IOException {
    logger.error(e.getMessage(), e);
    try {
      encodingFactory.writeObjectNullable(e);
    } catch (RpcException e1) {
      logger.error(Messages.CANNOT_GENERATE_RPCEXCPETION.getMessage());
      encodingFactory.writeIsNotNull(false);
    }
  }

  /**
   * Get the encoding factory.
   * 
   * @param name
   *          the encoding factory name
   * @param coreCodec
   *          the core codec
   * @return the encoding factory
   */
  protected RpcEncodingFactory getEncodingFactory(String name, CoreCodec coreCodec) {
    RpcEncodingFactory encodingFactory = encodingFactoryCache.get(name);
    if (null == encodingFactory) {
      encodingFactory = rpcServer.getEncodingFactory(name, coreCodec);
      encodingFactoryCache.put(name, encodingFactory);
    }
    return encodingFactory;
  }
}
