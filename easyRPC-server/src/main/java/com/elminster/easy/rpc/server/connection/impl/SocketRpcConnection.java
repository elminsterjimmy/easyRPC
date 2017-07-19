package com.elminster.easy.rpc.server.connection.impl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.Status;
import com.elminster.easy.rpc.codec.CoreCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol.Frame;
import com.elminster.easy.rpc.protocol.ResponseProtocol;
import com.elminster.easy.rpc.protocol.exception.UnexpectedFrameException;
import com.elminster.easy.rpc.protocol.impl.ProtocolFactoryImpl;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.worker.impl.WorkerJobId;
import com.elminster.easy.rpc.server.context.impl.InvokeeContextImpl;
import com.elminster.easy.rpc.server.context.impl.InvokeeContextImpl.InvokeeContextImplBuilder;
import com.elminster.easy.rpc.server.processor.RpcServiceProcessor;

/**
 * The Socket RPC Connection.
 * 
 * @author jinggu
 * @version 1.0
 */
public class SocketRpcConnection extends RpcConnectionImpl {

  private static final Logger logger = LoggerFactory.getLogger(SocketRpcConnection.class);

  private static final AtomicInteger SERIAL = new AtomicInteger(WorkerJobId.BIO_CONNECTION.getJobId());
  private final Socket socket;

  private final InetAddress localAddr;
  private final InetAddress remoteAddr;
  private final int localPort;
  private final int remotePort;

  {
    SERIAL.getAndIncrement();
  }

  public SocketRpcConnection(RpcServer server, Container container, Socket socket) {
    super(server, container, SERIAL.get(), "Socket Rpc Connection - " + Integer.toHexString(SERIAL.get()));
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
  protected void doRun() throws Exception {
    // init the core codec
    InvokeeContextImpl invokeContext = null;
    try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
      CoreCodec coreCodec = CoreCodecFactory.INSTANCE.getCoreCodec(in, out);

      InvokeeContextImplBuilder builder = new InvokeeContextImplBuilder();
      invokeContext = builder.withServerHost(localAddr).withClientHost(remoteAddr).withClientPort(remotePort).withServerPort(localPort).build();

      RpcEncodingFactory defaultEncodingFactory = getEncodingFactory("default", coreCodec);

      initializeBaseProtocols(defaultEncodingFactory);
      
      shakehand(defaultEncodingFactory);
      
      checkVersion(defaultEncodingFactory, invokeContext);

      while (!Thread.currentThread().isInterrupted()) {
        try {
          methodCall(defaultEncodingFactory, invokeContext, coreCodec);
        } catch (RpcException rpce) {
          confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
          writeRpcException(defaultEncodingFactory, rpce);
        }
      }
    } catch (IOException e) {
      if (e instanceof EOFException) {
        logger.warn(String.format(Messages.CLIENT_DISCONNECTED.getMessage(), invokeContext));
      } else {
        logger.error(e.getMessage(), e);
        throw e;
      }
    } catch (RpcException e) {
      logger.error(e.getMessage());
      throw e;
    }
  }

  @Override
  protected void shakehand(RpcEncodingFactory encodingFactory) throws IOException, RpcException {
    if (!confirmFrameProtocol.expact(Frame.FRAME_SHAKEHAND.getFrame())) {
      throw new UnexpectedFrameException(Frame.FRAME_SHAKEHAND.getFrame(), confirmFrameProtocol.getFrame());
    }
    super.shakehand(encodingFactory);
  }

  @Override
  protected void checkVersion(RpcEncodingFactory encodingFactory, InvokeeContextImpl invokeContext) throws IOException, RpcException {
    if (!confirmFrameProtocol.expact(Frame.FRAME_VERSION.getFrame())) {
      throw new UnexpectedFrameException(Frame.FRAME_VERSION.getFrame(), confirmFrameProtocol.getFrame());
    }
    super.checkVersion(encodingFactory, invokeContext);
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
  protected void methodCall(RpcEncodingFactory defaultEncodingFactory, InvokeeContextImpl invokeContext, CoreCodec coreCodec) throws IOException, RpcException {
    // start serve RPC calls
    if (!confirmFrameProtocol.expact(Frame.FRAME_HEADER.getFrame())) {
      throw new UnexpectedFrameException(Frame.FRAME_HEADER.getFrame(), confirmFrameProtocol.getFrame());
    }

    RpcEncodingFactory rpcEncodingFactory = handleRequestHeader(defaultEncodingFactory, invokeContext, coreCodec);
    RpcCall rpcCall = handleRequest(rpcEncodingFactory, invokeContext, coreCodec);
    ResponseProtocol responseProtocol;
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
    rpcCall.setStatus(Status.UNPROCCESSED);
    if (rpcCall.isAsyncCall()) {
      confirmFrameProtocol.nextFrame(Frame.FRAME_ASYNC_RESPONSE.getFrame());
      handleAsyncRpcCall(proccessor, rpcCall, defaultEncodingFactory, responseProtocol);
    } else {
      handleSyncRpcCall(proccessor, rpcCall, defaultEncodingFactory, responseProtocol);
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
  protected void handleSyncRpcCall(RpcServiceProcessor proccessor, RpcCall call, RpcEncodingFactory defaultEncodingFactory, ResponseProtocol responseProtocol)
      throws IOException, RpcException {
    proccessor.invoke(call);
    RpcCall result = proccessor.getResult(call, 10);
    writeResult(result, defaultEncodingFactory, responseProtocol);
  }

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
  protected void handleAsyncRpcCall(RpcServiceProcessor proccessor, RpcCall call, RpcEncodingFactory defaultEncodingFactory, ResponseProtocol responseProtocol)
      throws IOException, RpcException {
    proccessor.invoke(call);
    RpcServiceProcessor processor = container.getServiceProcessor();
    while (true) {
      if (!confirmFrameProtocol.expact(Frame.FRAME_ASYNC_REQUEST.getFrame())) {
        throw new UnexpectedFrameException(Frame.FRAME_ASYNC_REQUEST.getFrame(), confirmFrameProtocol.getFrame());
      }
      asyncProtocol.decode();

      String requestId = asyncProtocol.getRequestId();
      if (asyncProtocol.isCancel()) {
        cancelAsyncRpcCall(processor, requestId);
        continue;
      } else if (asyncProtocol.isQueryDone()) {
        boolean isDone = checkRpcCallIsDone(processor, requestId);
        asyncProtocol.setRequestId(requestId);
        asyncProtocol.setDone(isDone);
        asyncProtocol.setTimeout(0);
        asyncProtocol.encode();
      } else if (asyncProtocol.isGet()) {
        long timeout = asyncProtocol.getTimeout();
        RpcCall rpcCall = processor.getRpcCall(requestId);
        if (null == rpcCall) {
          confirmFrameProtocol.nextFrame(Frame.FRAME_UNAVAILABLE.getFrame());
        } else {
          if (Status.CANCELLED == rpcCall.getStatus()) {
            confirmFrameProtocol.nextFrame(Frame.FRAME_CANCELLED.getFrame());
          }
          RpcCall result = processor.getResult(rpcCall, timeout);
          if (null == result) {
            // timeout
            confirmFrameProtocol.nextFrame(Frame.FRAME_TIMEOUT.getFrame());
          } else {
            writeResult(result, defaultEncodingFactory, responseProtocol);
          }
        }
      }
    }
  }

  @Override
  protected void cleanUp() {
    super.cleanUp();
    // remove
    container.removeOpenConnection(this);
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
