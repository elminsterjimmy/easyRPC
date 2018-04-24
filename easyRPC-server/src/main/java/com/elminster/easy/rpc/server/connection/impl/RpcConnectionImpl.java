package com.elminster.easy.rpc.server.connection.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.thread.IJobMonitor;
import com.elminster.common.thread.Job;
import com.elminster.easy.rpc.call.RpcCall;
import com.elminster.easy.rpc.call.Status;
import com.elminster.easy.rpc.call.impl.RpcCallImpl;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.connection.RpcConnection;
import com.elminster.easy.rpc.data.Request;
import com.elminster.easy.rpc.data.Response;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.exception.VersionCompatibleException;
import com.elminster.easy.rpc.protocol.RequestProtocol;
import com.elminster.easy.rpc.protocol.ResponseProtocol;
import com.elminster.easy.rpc.protocol.impl.RequestProtocolImpl;
import com.elminster.easy.rpc.protocol.impl.ResponseProtocolImpl;
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

  protected RequestProtocol requestProtocol;
  protected ResponseProtocol responseProtocol;

  // cache the encoding factory since they're heavy objects.
  private Map<String, RpcEncodingFactory> encodingFactoryCache = new ConcurrentHashMap<>();

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
   * @param encodingFactory
   *          the encoding factory
   */
  protected void initializeBaseProtocols(RpcEncodingFactory encodingFactory) {
    requestProtocol = new RequestProtocolImpl(encodingFactory);
    responseProtocol = new ResponseProtocolImpl(encodingFactory);
  }

  /**
   * Check the version between server and client.
   * 
   * @param encodingFactory
   *          the encoding factory
   * @param invokeContext
   *          the invokee context
   * @throws RpcException
   *           on error
   */
  protected void checkVersion(String clientVersion, InvokeeContextImpl invokeContext) throws RpcException {
    String serverVersion = rpcServer.getVersion();
    invokeContext.setInvokerVersion(clientVersion);
    invokeContext.setInvokeeVersion(serverVersion);

    // send server version
    if (!VersionChecker.compatible(clientVersion, serverVersion)) {
      if (rpcServer.isVersionCheck()) {
        // return exception and disconnection
        String msg = String.format("Incompatible versions! Server version is [%s] but Client version is [%s].", serverVersion, clientVersion);
        throw new VersionCompatibleException(msg);
      }
    }
  }

  protected RpcCall toRpcCall(Request request, InvokeeContextImpl invokeContext) throws IOException, RpcException {
    return new RpcCallImpl(request, invokeContext);
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
  protected void methodCall(Request request, InvokeeContextImpl invokeContext) throws IOException, RpcException {
    RpcCall rpcCall = toRpcCall(request, invokeContext);
    RpcServiceProcessor proccessor = container.getServiceProcessor();
    rpcCall.setStatus(Status.UNPROCCESSED);
    proccessor.invoke(rpcCall);
  }


  /**
   * Write the response.
   * 
   * @param response
   *          the response
   * @throws IOException
   *           on error
   * @throws RpcException
   *           on error
   */
  protected void writeResponse(Response response) throws IOException, RpcException {
    if (logger.isDebugEnabled()) {
      logger.debug("write response [{}].", response);
    }
    responseProtocol.encode(response);
  }

  /**
   * Write an exception.
   * 
   * @param e
   *          the exception
   * @param message
   *          the message
   * @throws IOException
   *           on error
   */
  protected void writeException(Throwable e, String message) throws IOException {
    RpcException rpce = new RpcException(message, e);
    writeRpcException(rpce);
  }

  /**
   * Write a RpcException.
   * 
   * @param e
   *          the RpcException
   * @throws IOException
   *           on error
   */
  protected void writeRpcException(RpcException e) throws IOException {
    logger.error(e.getMessage(), e);
    Response response = new Response();
    response.setReqeustId("0xFF");
    response.setVoid(false);
    response.setReturnValue(e);
    try {
      writeResponse(response);
    } catch (RpcException e1) {
      // should never happen
      logger.warn(e1.getMessage());
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
  protected RpcEncodingFactory getEncodingFactory(Codec coreCodec) {
    RpcEncodingFactory encodingFactory = encodingFactoryCache.get(coreCodec.getName());
    if (null == encodingFactory) {
      encodingFactory = rpcServer.getEncodingFactory(coreCodec);
      encodingFactoryCache.put(coreCodec.getName(), encodingFactory);
    }
    return encodingFactory;
  }
}
