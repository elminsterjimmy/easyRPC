package com.elminster.easy.rpc.client.container.connection;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.exception.ObjectInstantiationExcption;
import com.elminster.common.util.BinaryUtil;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.client.RpcClient;
import com.elminster.easy.rpc.client.connection.Connection;
import com.elminster.easy.rpc.client.container.impl.BioContainerImpl;
import com.elminster.easy.rpc.client.context.impl.InvokerContextImpl;
import com.elminster.easy.rpc.client.context.impl.InvokerContextImpl.InvokerContextImplBuilder;
import com.elminster.easy.rpc.client.processor.RpcClientProcessor;
import com.elminster.easy.rpc.client.processor.impl.BioRpcClientProcessor;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.codec.impl.CoreCodecFactory;
import com.elminster.easy.rpc.context.ConnectionEndpoint;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryNotFoundException;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryRepository;
import com.elminster.easy.rpc.encoding.impl.DefaultRpcEncodingFactory;
import com.elminster.easy.rpc.exception.ConnectionException;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.registery.SocketFactoryRegsitery;
import com.elminster.easy.rpc.request.Header;
import com.elminster.easy.rpc.request.Response;
import com.elminster.easy.rpc.request.RpcRequest;
import com.elminster.easy.rpc.request.impl.HeaderImpl;
import com.elminster.easy.rpc.request.impl.RpcRequestImpl;
import com.elminster.easy.rpc.serializer.Serializer;
import com.elminster.easy.rpc.serializer.impl.HeaderSerializer;
import com.elminster.easy.rpc.serializer.impl.ResponseSerializer;
import com.elminster.easy.rpc.serializer.impl.RpcRequestSerializer;

public class BioConnectionImpl implements Connection {
  
  private static final Logger logger = LoggerFactory.getLogger(BioConnectionImpl.class);
  
  private final RpcClient rpcClient;
  private final ConnectionEndpoint endpoint;
  private final BioContainerImpl container;
  private Socket socket;
  private InputStream in;
  private OutputStream out;
  private RpcClientProcessor processor;
  
  public BioConnectionImpl(RpcClient rpcClient, ConnectionEndpoint endpoint, BioContainerImpl container) {
    this.rpcClient = rpcClient;
    this.endpoint = endpoint;
    this.container = container;
  }

  public void connect() throws ConnectionException {
    InvokerContextImpl invokerContext = null;
    try {
      socket = SocketFactoryRegsitery.INSTANCE.getSocketFactory(rpcClient.getRpcContext()).createClientSocket(endpoint);
      setupSocket(socket);
      invokerContext = generateInvokerContext(socket);

      in = socket.getInputStream();
      out = new BufferedOutputStream(socket.getOutputStream());

      Codec codec = CoreCodecFactory.INSTANCE.getCoreCodec(in, out);
      final RpcEncodingFactory encodingFactory = rpcClient.getEncodingFactory().cloneEncodingFactory();
      encodingFactory.setCodec(codec);
      
      RpcEncodingFactoryRepository repository = new RpcEncodingFactoryRepository() {

        @Override
        public RpcEncodingFactory getRpcEncodingFactory(String encodingFactoryName, Codec codec) throws RpcEncodingFactoryNotFoundException {
          return encodingFactory;
        }

        @Override
        public void addRpcEncodingFactory(RpcEncodingFactory encodingFactory) {
        }

        @Override
        public void removeRpcEncodingFactory(String encodingFactoryName) {
        }
      };
      Serializer<Header> headerSerializer = new HeaderSerializer(repository, codec);
      HeaderImpl header = new HeaderImpl();
      header.setVersion(this.rpcClient.getVersion());
      header.setRequestType(Header.SYNC_REQUEST);
      byte[] mHeader = headerSerializer.serialize(header);
      
      RpcRequestImpl request = new RpcRequestImpl();
      request.setEncoding(DefaultRpcEncodingFactory.ENCODING_FACTORY_NAME);
      request.setMethodName("ping");
      request.setServiceName("ping");
      request.setServiceVersion("1.0.0");
      request.setRequestId("PING");
      
      Serializer<RpcRequest> reqSerializer = new RpcRequestSerializer(repository, codec);
      byte[] mReq = reqSerializer.serialize(request);
      encodingFactory.writeObjectNullable(BinaryUtil.concatBytes(mHeader, mReq));
      encodingFactory.flush();
      
      byte[] resMsg = (byte[]) encodingFactory.readObjectNullable();
      ByteBuffer byteBuffer = ByteBuffer.wrap(resMsg);
      Serializer<Response> resSerializer = new ResponseSerializer(repository, codec);
      Response res = resSerializer.deserialize(byteBuffer);
      Object rtn = res.getReturnValue();
      if (rtn instanceof Boolean) {
        processor = new BioRpcClientProcessor(encodingFactory, invokerContext, this);
      } else if (rtn instanceof RpcException) {
        throw (RpcException) rtn;
      } else {
        throw new RpcException("Server is not pingable!");
      }
      
    } catch (IOException | ObjectInstantiationExcption | RpcException e) {
      closeStreams();
      if (e instanceof EOFException) {
        String msg = String.format("Connection with Rpc Server is broken. Context [%s]", invokerContext);
        logger.error(msg);
        throw new ConnectionException(msg, e);
      }
      throw new ConnectionException(String.format("Cannot create connection to server [%s].", endpoint), e);
    }
  }
  
  private void closeStreams() {
    try {
      if (null != in) {
        in.close();
      }
      if (null != out) {
        out.close();
      }
      if (null != socket) {
        socket.close();
      }
    } catch (IOException e) {
      logger.warn("Cannot close streams!", e);
    }
  }

  private InvokerContextImpl generateInvokerContext(Socket socket) {
    return new InvokerContextImplBuilder().withClientHost(socket.getLocalAddress()).withClientPort(socket.getLocalPort()).withServerHost(socket.getInetAddress())
        .withServerPort(socket.getPort()).build();
  }

  private void setupSocket(Socket socket) {
    RpcContext context = rpcClient.getRpcContext();
    Integer timeout = context.getClientTimeout();
    Boolean useTcpNoDelay = context.getClientTcpNoDelay();
    if (null != timeout) {
      try {
        int otimeout = socket.getSoTimeout();
        socket.setSoTimeout(timeout.intValue());
        logger.debug(String.format("set socket timeout from [%d] to [%d].", otimeout, timeout.intValue()));
      } catch (SocketException e) {
        logger.warn("failed to set socket timeout.", e);
      }
    }
    if (null != useTcpNoDelay) {
      try {
        boolean oTpcDelay = socket.getTcpNoDelay();
        socket.setTcpNoDelay(useTcpNoDelay.booleanValue());
        logger.debug(String.format("set socket timeout from [%b] to [%b].", oTpcDelay, useTcpNoDelay.booleanValue()));
      } catch (SocketException e) {
        logger.warn("failed to set socket TCP delay.", e);
      }
    }
  }

  @Override
  public void disconnect() throws IOException {
    closeStreams();
    container.removeConnection(this);
  }

  @Override
  public Object invokeService(RpcCall rpcCall) throws Throwable {
    return processor.invokeService(rpcCall);
  }

  @Override
  public boolean isConnected() {
    return !socket.isClosed();
  }
  
  public RpcClient getRpcClient() {
    return this.rpcClient;
  }
}
