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
import com.elminster.easy.rpc.protocol.AsyncResponseProtocol;
import com.elminster.easy.rpc.protocol.ResponseProtocol;
import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol.Frame;
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
        methodCall(defaultEncodingFactory, invokeContext, coreCodec);
      }
    } catch (IOException e) {
      if (e instanceof EOFException) {
        logger.warn(String.format(Messages.CLIENT_DISCONNECTED.getMessage(), invokeContext));
      } else {
        throw e;
      }
    } catch (RpcException e) {
      logger.error(e.getMessage());
      throw e;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void handleSyncRpcCall(RpcServiceProcessor proccessor, RpcCall call, RpcEncodingFactory defaultEncodingFactory, ResponseProtocol responseProtocol)
      throws IOException, RpcException {
    proccessor.invoke(call);
    RpcCall result = proccessor.getResult(call, 10);
    writeResult(result, defaultEncodingFactory, responseProtocol);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void handleAsyncRpcCall(RpcServiceProcessor proccessor, RpcCall call, RpcEncodingFactory defaultEncodingFactory, ResponseProtocol responseProtocol) throws IOException, RpcException {
    proccessor.invoke(call);
    AsyncResponseProtocol asyncProtocol;
    try {
      asyncProtocol = (AsyncResponseProtocol) ProtocolFactoryImpl.INSTANCE.createProtocol(AsyncResponseProtocol.class, defaultEncodingFactory);
    } catch (ObjectInstantiationExcption e) {
      // unexpected error
      confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
      String message = "Cannot instantiate async response protocols, and this should NOT happened!";
      writeException(defaultEncodingFactory, e, message);
      return; // start over
    }
    RpcServiceProcessor processor = container.getServiceProcessor();
    while (true) {
      // TODO
      try {
        asyncProtocol.decode();
      } catch (RpcException e) {
        confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
        writeRpcException(defaultEncodingFactory, e);
      }

      String requestId = asyncProtocol.getRequestId();
      if (asyncProtocol.isCancel()) {
        cancelAsyncRpcCall(processor, requestId);
        continue;
      } else if (asyncProtocol.isQueryDone()) {
        boolean isDone = checkRpcCallIsDone(processor, requestId);
        asyncProtocol.setRequestId(requestId);
        asyncProtocol.setDone(isDone);
        asyncProtocol.setTimeout(0);
        try {
          asyncProtocol.encode();
        } catch (RpcException e) {
          confirmFrameProtocol.nextFrame(Frame.FRAME_EXCEPTION.getFrame());
          writeRpcException(defaultEncodingFactory, e);
        }
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
