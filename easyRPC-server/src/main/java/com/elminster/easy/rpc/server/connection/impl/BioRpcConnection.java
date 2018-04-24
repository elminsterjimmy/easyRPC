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

import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.data.Async;
import com.elminster.easy.rpc.data.Request;
import com.elminster.easy.rpc.data.Response;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
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
public class BioRpcConnection extends RpcConnectionImpl {

  private static final Logger logger = LoggerFactory.getLogger(BioRpcConnection.class);

  private static final AtomicInteger SERIAL = new AtomicInteger(WorkerJobId.BIO_CONNECTION.getJobId());
  private final Socket socket;

  private final InetAddress localAddr;
  private final InetAddress remoteAddr;
  private final int localPort;
  private final int remotePort;

  {
    SERIAL.getAndIncrement();
  }

  public BioRpcConnection(RpcServer server, Container container, Socket socket) {
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
    Codec coreCodec = null;
    try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
      coreCodec = CoreCodecFactory.INSTANCE.getCoreCodec(in, out);

      InvokeeContextImplBuilder builder = new InvokeeContextImplBuilder();
      invokeContext = builder.withServerHost(localAddr).withClientHost(remoteAddr).withClientPort(remotePort).withServerPort(localPort).build();

      RpcEncodingFactory encodingFactory = getEncodingFactory(coreCodec);

      initializeBaseProtocols(encodingFactory);
      while (!Thread.currentThread().isInterrupted()) {
        try {
          Request request = requestProtocol.decode();
          if (null != request) {
            checkVersion(request.getVersion(), invokeContext);
            methodCall(request, invokeContext);
            RpcServiceProcessor proccessor = container.getServiceProcessor();
            Response response = new Response();
            if (Async.ASYNC == request.getAsync()) {
              response.setReqeustId(request.getRequestId());
              response.setVoid(false);
              response.setReturnValue("OK");
            } else {
              RpcCall result = proccessor.getResult(request.getRequestId(), 10);
              response.setReqeustId(result.getRequestId());
              response.setVoid(result.isVoidReturn());
              response.setReturnValue(result.getResult().getReturnValue());
            }
            writeResponse(response);
          }
        } catch (RpcException rpce) {
          writeRpcException(rpce);
        }
      }
    } catch (IOException e) {
      if (e instanceof EOFException) {
        logger.warn(String.format(Messages.CLIENT_DISCONNECTED.getMessage(), invokeContext));
      } else {
        logger.error(e.getMessage(), e);
        throw e;
      }
    } finally {
      if (null != coreCodec) {
        coreCodec.close();
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
