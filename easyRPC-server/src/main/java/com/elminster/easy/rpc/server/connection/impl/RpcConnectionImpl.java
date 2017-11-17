package com.elminster.easy.rpc.server.connection.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.thread.IJobMonitor;
import com.elminster.common.thread.Job;
import com.elminster.common.threadpool.ThreadPool;
import com.elminster.common.threadpool.ThreadPoolConfiguration;
import com.elminster.easy.rpc.call.ReturnResult;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.Status;
import com.elminster.easy.rpc.call.impl.RpcCallImpl;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryNotFoundException;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryRepository;
import com.elminster.easy.rpc.encoding.impl.DefaultRpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.exception.VersionCompatibleException;
import com.elminster.easy.rpc.request.AsyncQueryRequest;
import com.elminster.easy.rpc.request.Header;
import com.elminster.easy.rpc.request.Request;
import com.elminster.easy.rpc.request.Response;
import com.elminster.easy.rpc.request.RpcRequest;
import com.elminster.easy.rpc.request.impl.AsyncQueryResponseImpl;
import com.elminster.easy.rpc.request.impl.ResponseImpl;
import com.elminster.easy.rpc.serializer.Serializer;
import com.elminster.easy.rpc.serializer.impl.AsyncQueryRequestSerializer;
import com.elminster.easy.rpc.serializer.impl.AsyncQueryResponseSerializer;
import com.elminster.easy.rpc.serializer.impl.HeaderSerializer;
import com.elminster.easy.rpc.serializer.impl.ResponseSerializer;
import com.elminster.easy.rpc.serializer.impl.RpcRequestSerializer;
import com.elminster.easy.rpc.server.RpcServer;
import com.elminster.easy.rpc.server.container.Container;
import com.elminster.easy.rpc.server.container.worker.impl.WorkerJobId;
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

  // cache the encoding factory since they're heavy objects.
  private Map<EncodingFacotryKey, RpcEncodingFactory> encodingFactoryCache = new ConcurrentHashMap<>();

  class EncodingFacotryKey {

    public EncodingFacotryKey(String name, Codec codec) {
      this.name = name;
      this.codec = codec;
    }

    String name;
    Codec codec;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((codec == null) ? 0 : codec.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      EncodingFacotryKey other = (EncodingFacotryKey) obj;
      if (codec == null) {
        if (other.codec != null)
          return false;
      } else if (!codec.equals(other.codec))
        return false;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      return true;
    }
  }

  // the thread pool that deal with the async timeout get
  private final ThreadPool threadPool = new ThreadPool(ThreadPoolConfiguration.INSTANCE);

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
      monitor.beginJob(this.getName(), 1);
      doRun();
      return monitor.done();
    } finally {
      cleanUp();
    }
  }

  protected void cleanUp() {
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
   * Read the header.
   * 
   * @param repository
   *          the encoding factory repository
   * @param codec
   *          the codec
   * @param context
   *          the context
   * @return the header
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on error
   */
  protected Header readHeader(ByteBuffer byteBuffer, RpcEncodingFactoryRepository repository, Codec codec, InvokeeContextImpl context) throws IOException, RpcException {
    Serializer<Header> headerSerializer = new HeaderSerializer(repository, codec);
    return headerSerializer.deserialize(byteBuffer);
  }

  /**
   * Read the sync request.
   * 
   * @param repository
   *          the encoding factory repository
   * @param codec
   *          the codec
   * @param context
   *          the context
   * @return the request
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on error
   */
  protected RpcRequest readRequest(ByteBuffer byteBuffer, RpcEncodingFactoryRepository repository, Codec codec, InvokeeContextImpl context) throws IOException, RpcException {
    Serializer<RpcRequest> requestSerializer = new RpcRequestSerializer(repository, codec);
    return requestSerializer.deserialize(byteBuffer);
  }

  /**
   * Read the async request.
   * 
   * @param repository
   *          the encoding factory repository
   * @param codec
   *          the codec
   * @param context
   *          the context
   * @return the request
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on error
   */
  protected AsyncQueryRequest readAsyncRequest(ByteBuffer byteBuffer, RpcEncodingFactoryRepository repository, Codec codec, InvokeeContextImpl context)
      throws IOException, RpcException {
    Serializer<AsyncQueryRequest> requestSerializer = new AsyncQueryRequestSerializer(repository, codec);
    return requestSerializer.deserialize(byteBuffer);
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
   *           on error
   */
  protected void checkVersion(Header header, InvokeeContextImpl invokeContext) throws IOException, RpcException {
    String clientVersion = header.getVersion();
    String serverVersion = rpcServer.getVersion();
    invokeContext.setInvokerVersion(clientVersion);
    invokeContext.setInvokeeVersion(serverVersion);

    if (!VersionChecker.compatible(clientVersion, serverVersion)) {
      if (rpcServer.isVersionCheck()) {
        // return exception and disconnection
        String msg = String.format("Incompatible versions! Server version is [%s] but Client version is [%s].", serverVersion, clientVersion);
        logger.error(msg);
        throw new VersionCompatibleException(msg);
      }
    }
  }

  /**
   * Check the RPC call is done or not.
   * 
   * @param processor
   *          the processor
   * @param requestId
   *          the RPC call request id
   * @return the RPC call is done or not
   */
  protected boolean checkRpcCallIsDone(RpcServiceProcessor processor, String requestId) {
    RpcCall call = processor.getRpcCall(requestId);
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
   * @param call
   *          the RPC call
   * @param codec
   *          the codec
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on error
   */
  protected void writeResult(RpcCall call, RpcEncodingFactoryRepository repository, Codec codec) throws IOException, RpcException {
    ReturnResult result = call.getResult();
    Class<?> returnType = result.getReturnType();
    Object returnValue = result.getReturnValue();
    ResponseImpl response = new ResponseImpl();
    response.setRequestId(call.getRequestId());
    response.setReturnValue(returnValue);
    response.setVoidCall(Void.class == returnType || void.class == returnType);
    RpcRequest request = call.getRequest();
    response.setEncoding(request.getEncoding());
    writeResponse(response, repository, codec);
  }

  /**
   * Write the response.
   * 
   * @param response
   *          the response
   * @param repository
   *          the repository
   * @param codec
   *          the codec
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on error
   */
  protected void writeResponse(Response response, RpcEncodingFactoryRepository repository, Codec codec) throws IOException, RpcException {
    Serializer<Response> serializer = new ResponseSerializer(repository, codec);
    byte[] message = serializer.serialize(response);
    RpcEncodingFactory defaultEncodingFactory = repository.getRpcEncodingFactory(DefaultRpcEncodingFactory.ENCODING_FACTORY_NAME, codec);
    defaultEncodingFactory.writeObjectNullable(message);
  }

  /**
   * Write an exception.
   * 
   * @param encodingFactory
   *          the encoding factory
   * @param e
   *          the exception
   * @throws IOException
   *           on error
   */
  protected void writeException(RpcEncodingFactoryRepository repository, Codec codec, Throwable e, Request request) throws IOException {
    writeException(repository, codec, e, e.getMessage(), request);
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
  protected void writeException(RpcEncodingFactoryRepository repository, Codec codec, Throwable e, String message, Request request) throws IOException {
    RpcException rpce;
    if (e instanceof RpcException) {
      rpce = (RpcException) e;
    } else {
      rpce = new RpcException(message, e);
    }
    writeRpcException(repository, codec, rpce, request);
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
  protected void writeRpcException(RpcEncodingFactoryRepository repository, Codec codec, RpcException e, Request request) throws IOException {
    try {
      ResponseImpl response = new ResponseImpl();
      response.setEncoding(DefaultRpcEncodingFactory.ENCODING_FACTORY_NAME);
      response.setReturnValue(e);
      if (null != request) {
        response.setRequestId(request.getRequestId());
        response.setEncoding(request.getEncoding());
      }
      writeResponse(response, repository, codec);
    } catch (RpcException e1) {
      writeRpcException(repository, codec, e1, request);
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
  protected RpcEncodingFactory getEncodingFactory(String name, Codec coreCodec) throws RpcEncodingFactoryNotFoundException {
    RpcEncodingFactory encodingFactory = encodingFactoryCache.get(new EncodingFacotryKey(name, coreCodec));
    if (null == encodingFactory) {
      encodingFactory = rpcServer.getEncodingFactory(name, coreCodec);
      if (null != encodingFactory) {
        encodingFactoryCache.put(new EncodingFacotryKey(name, coreCodec), encodingFactory);
      } else {
        throw new RpcEncodingFactoryNotFoundException(name);
      }
    }
    return encodingFactory;
  }

  protected void heanleAsyncRequest(AsyncQueryRequest request, RpcEncodingFactoryRepository repository, Codec codec, InvokeeContextImpl context) throws IOException, RpcException {
    String id2Request = request.getId2Request();
    String reqId = request.getRequestId();
    RpcServiceProcessor processor = container.getServiceProcessor();
    AsyncQueryResponseImpl response = new AsyncQueryResponseImpl();
    response.setId2Request(id2Request);
    response.setRequestId(reqId);
    if (request.isCancel()) {
      boolean cancelled = cancelAsyncRpcCall(processor, id2Request);
      response.setCancelled(cancelled);
      writeAsyncResponse(response, repository, codec);
    } else if (request.isQueryDone()) {
      boolean isDone = checkRpcCallIsDone(processor, id2Request);
      response.setDone(isDone);
      writeAsyncResponse(response, repository, codec);
    } else if (request.isRequestGet()) {
      long timeout = request.getTimeout();
      RpcCall call = processor.getRpcCall(id2Request);
      // let the notifier to do the work in another thread
      Notifier notifier = new Notifier(call, timeout, processor, repository, codec);
      threadPool.execute(notifier);
    } else {
      throw new RpcException(String.format("Unknow Async Request Action [%d]!", request.getAction()));
    }
  }

  private void writeAsyncResponse(AsyncQueryResponseImpl response, RpcEncodingFactoryRepository repository, Codec codec) throws IOException, RpcException {
    AsyncQueryResponseSerializer serializer = new AsyncQueryResponseSerializer(repository, codec);
    byte[] mRes = serializer.serialize(response);
    RpcEncodingFactory defaultEncodingFactory = repository.getRpcEncodingFactory(DefaultRpcEncodingFactory.ENCODING_FACTORY_NAME, codec);
    defaultEncodingFactory.writeObjectNullable(mRes);
  }

  class Notifier extends Job {
    private final RpcCall rpcCall;
    private final long timeout;
    private final RpcServiceProcessor processor;
    private final Codec codec;
    private final RpcEncodingFactoryRepository repository;

    public Notifier(RpcCall rpcCall, long timeout, RpcServiceProcessor processor, RpcEncodingFactoryRepository repository, Codec codec) {
      super(WorkerJobId.NIO_NOTIFIER.getJobId(), String.format("RPC Call Notifier for RPC Call [%s] and timeout [%d]", rpcCall, timeout));
      this.rpcCall = rpcCall;
      this.timeout = timeout;
      this.processor = processor;
      this.repository = repository;
      this.codec = codec;
    }

    @Override
    protected JobStatus doWork(IJobMonitor monitor) throws Throwable {
      RpcCall result = processor.getResult(rpcCall, timeout);
      ResponseImpl response = new ResponseImpl();
      switch (result.getStatus()) {
      case CANCELLED:
        response.setCancelled(true);
        break;
      case PROCESSED:
      case EXCEPTION:
        response.setEncoding(result.getRequest().getEncoding());
        response.setRequestId(result.getRequestId());
        response.setReturnValue(result.getResult().getReturnValue());
        Class<?> returnType = result.getResult().getReturnType();
        response.setVoidCall(Void.class == returnType || void.class == returnType);
        break;
      default:
        response.setTimeout(true);
        processor.cancelRpcCall(rpcCall);
      }
      writeResponse(response, repository, codec);
      return monitor.done();
    }

  }

  /**
   * Generate the RPC call.
   * 
   * @param request
   *          the request
   * @param context
   *          the context
   * @return the RPC call
   * @throws IOException
   *           on IO error
   * @throws RpcException
   *           on RPC error
   */
  protected RpcCallImpl generateRpcCall(Request request, InvokeeContextImpl context) throws IOException, RpcException {
    return new RpcCallImpl((RpcRequest) request, context);
  }

  /**
   * Invoke a RPC call.
   * 
   * @param request
   *          the request
   * @param repository
   *          the RPC encoding factory repository
   * @param codec
   *          the codec
   * @param context
   *          the context
   * @throws IOException
   *           on error
   */
  protected void invokeRpcCall(Request request, RpcEncodingFactoryRepository repository, Codec codec, InvokeeContextImpl context) throws IOException, RpcException {
    // start serve RPC calls
    RpcCall call = generateRpcCall(request, context);
    RpcServiceProcessor proccessor = container.getServiceProcessor();
    call.setStatus(Status.UNPROCCESSED);
    if (call.isAsyncCall()) {
      handleAsyncRpcCall(proccessor, call);
    } else {
      handleSyncRpcCall(proccessor, call, repository, codec);
    }
  }

  /**
   * Handle the sync RPC call.
   * 
   * @param proccessor
   *          the processor
   * @param call
   *          the RPC call
   * @param repository
   *          the RPC encoding factory repository
   * @param codec
   *          the codec
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on error
   */
  protected void handleSyncRpcCall(RpcServiceProcessor proccessor, RpcCall call, RpcEncodingFactoryRepository repository, Codec codec) throws IOException, RpcException {
    proccessor.invoke(call);
    RpcCall result = proccessor.getResult(call, 10);
    writeResult(result, repository, codec);
  }

  /**
   * Handle the async RPC call.
   * 
   * @param proccessor
   *          the processor
   * @param call
   *          the RPC call
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on error
   */
  protected void handleAsyncRpcCall(RpcServiceProcessor proccessor, RpcCall call) throws IOException, RpcException {
    proccessor.invoke(call);
  }

  protected void handleRequests(RpcEncodingFactoryRepository repository, Codec codec, InvokeeContextImpl context) throws IOException {
    Request request = null;
    try {
      RpcEncodingFactory defaultEncodingFactory = repository.getRpcEncodingFactory(DefaultRpcEncodingFactory.ENCODING_FACTORY_NAME, codec);
      byte[] message = (byte[]) defaultEncodingFactory.readObjectNullable();
      ByteBuffer byteBuffer = ByteBuffer.wrap(message);
      Header header = readHeader(byteBuffer, repository, codec, context);
      checkVersion(header, context);

      switch (header.getRequestType()) {
      case Header.SYNC_REQUEST:
        request = readRequest(byteBuffer, repository, codec, context);
        invokeRpcCall(request, repository, codec, context);
        break;
      case Header.ASYNC_REQUEST:
        request = readAsyncRequest(byteBuffer, repository, codec, context);
        heanleAsyncRequest((AsyncQueryRequest) request, repository, codec, context);
        break;
      default:
        throw new RpcException(String.format("Unknown Request Type [%d].", header.getRequestType()));
      }
    } catch (IOException ioe) {
      // throw all IO exceptions that make the connection disconnect
      throw ioe;
    } catch (Exception e) {
      // write all other exceptions to the client
      writeException(repository, codec, e, request);
    }
  }
}
