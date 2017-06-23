package com.elminster.easy.rpc.codec.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.exception.RpcServerException;
import com.elminster.easy.rpc.registery.CoreServiceRegistry;
import com.elminster.easy.rpc.util.RpcUtil;

import static com.elminster.easy.rpc.codec.CodecConst.IS_NULL;
import static com.elminster.easy.rpc.codec.CodecConst.NOT_NULL;

/**
 * The RPC Server Exception Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcServerExceptionCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(RpcServerExceptionCodec.class);

  /** the RPC util. */
  private static final RpcUtil rpcUtil = CoreServiceRegistry.INSTANCE.getRpcUtil();

  private static final byte HAS_CAUSE = 1;
  private static final byte NO_CAUSE = 0;
  private static final byte HAS_CODEC = 1;
  private static final byte NO_CODEC = 0;
  private static final byte HAS_MORE = 1;
  private static final byte NO_MORE = 0;

  /**
   * {@inheritDoc}
   */
  public Object decode(InputStream iStream, RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (rpcUtil.readByte(iStream) == IS_NULL) {
        return null;
      }
      String errorMessage = rpcUtil.readStringAsciiNullable(iStream);
      int errorCode = rpcUtil.readIntBigEndian(iStream);

      RpcServerException serverException = new RpcServerException(errorMessage, errorCode);
      serverException.setCausedByClassName(rpcUtil.readStringAsciiNullable(iStream));
      serverException.setCausedByStackTrace(rpcUtil.readStringAsciiNullable(iStream));

      if (rpcUtil.readByte(iStream) == HAS_CAUSE) {
        if (rpcUtil.readByte(iStream) == HAS_CODEC) {
          Throwable cause = (Throwable) encodingFactory.readObjectNullable(iStream);
          if (cause != null) {
            serverException.initCause(cause);
          }
        } else {
          List<StackTraceElement> stl = new ArrayList<>();
          int size = rpcUtil.readIntBigEndian(iStream);
          for (int i = 0; i < size; i++) {
            stl.add(new StackTraceElement(rpcUtil.readStringAsciiNullable(iStream), rpcUtil.readStringAsciiNullable(iStream), rpcUtil.readStringAsciiNullable(iStream),
                rpcUtil.readIntBigEndian(iStream)));
          }
          serverException.setStackTrace((StackTraceElement[]) stl.toArray(new StackTraceElement[stl.size()]));
          if (rpcUtil.readByte(iStream) == HAS_MORE) {
            RpcServerException next = (RpcServerException) decode(iStream, encodingFactory);
            serverException.initCause(next);
          }
        }
      }
      return serverException;
    } catch (IOException e) {
      String message = "Could not decode KisRpcServerException - " + e;
      logger.error(message, e);
      throw new RpcException(message);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void encode(final OutputStream oStream, final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (value == null) {
        rpcUtil.writeByte(oStream, IS_NULL);
        return;
      }
      rpcUtil.writeByte(oStream, NOT_NULL);

      RpcServerException serverException = (RpcServerException) value;

      rpcUtil.writeStringAsciiNullable(oStream, serverException.getMessage());

      rpcUtil.writeIntBigEndian(oStream, serverException.getErrorCode());

      rpcUtil.writeStringAsciiNullable(oStream, serverException.getCausedByClassName());

      rpcUtil.writeStringAsciiNullable(oStream, serverException.getCausedByStackTrace());

      Throwable cause = serverException.getCause();
      if (cause != null) {
        rpcUtil.writeByte(oStream, HAS_CAUSE);

        String causeName = cause.getClass().getName();
        RpcCodec codec = encodingFactory != null ? encodingFactory.getEncodingObject(causeName, TypeCategory.JAVA) : null;
        if (codec != null) {
          rpcUtil.writeByte(oStream, HAS_CODEC);
          encodingFactory.writeObjectNullable(oStream, cause);
        } else {
          rpcUtil.writeByte(oStream, NO_CODEC);
          StackTraceElement[] st = cause.getStackTrace();

          rpcUtil.writeIntBigEndian(oStream, st.length);
          for (StackTraceElement ste : st) {
            rpcUtil.writeStringAsciiNullable(oStream, ste.getClassName());
            rpcUtil.writeStringAsciiNullable(oStream, ste.getMethodName());
            rpcUtil.writeStringAsciiNullable(oStream, ste.getFileName());
            rpcUtil.writeIntBigEndian(oStream, ste.getLineNumber());
          }
          cause = cause.getCause();
          if (cause != null) {
            rpcUtil.writeByte(oStream, HAS_MORE);
            RpcServerException next = new RpcServerException(cause.getClass().getCanonicalName() + ":" + cause.getMessage(), cause, serverException.getErrorCode());
            encode(oStream, next, encodingFactory);
          } else {
            rpcUtil.writeByte(oStream, NO_MORE);
          }
        }
      } else {
        rpcUtil.writeByte(oStream, NO_CAUSE);
      }
    } catch (IOException e) {
      String message = "Could not encode KisRpcServerException - " + e;
      logger.error(message, e);
      throw new RpcException(message);
    }
  }
}
