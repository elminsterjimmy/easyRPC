package com.elminster.easy.rpc.codec.impl;

import static com.elminster.easy.rpc.codec.CodecConst.IS_NULL;
import static com.elminster.easy.rpc.codec.CodecConst.NOT_NULL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;

/**
 * The RPC Server Exception Codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcServerExceptionCodec implements RpcCodec {

  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(RpcServerExceptionCodec.class);

  private static final byte HAS_CAUSE = 1;
  private static final byte NO_CAUSE = 0;
  private static final byte HAS_CODEC = 1;
  private static final byte NO_CODEC = 0;
  private static final byte HAS_MORE = 1;
  private static final byte NO_MORE = 0;

  /**
   * {@inheritDoc}
   */
  public Object decode(RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (encodingFactory.readInt8() == IS_NULL) {
        return null;
      }
      String errorMessage = encodingFactory.readStringNullable();

      RpcException serverException = new RpcException(errorMessage);
      serverException.setCausedByClassName(encodingFactory.readStringNullable());
      serverException.setCausedByStackTrace(encodingFactory.readStringNullable());

      if (encodingFactory.readInt8() == HAS_CAUSE) {
        if (encodingFactory.readInt8() == HAS_CODEC) {
          Throwable cause = (Throwable) encodingFactory.readObjectNullable();
          if (cause != null) {
            serverException.initCause(cause);
          }
        } else {
          List<StackTraceElement> stl = new ArrayList<>();
          int size = encodingFactory.readInt32();
          for (int i = 0; i < size; i++) {
            stl.add(new StackTraceElement(encodingFactory.readStringNullable(), encodingFactory.readStringNullable(), encodingFactory.readStringNullable(),
                encodingFactory.readInt32()));
          }
          serverException.setStackTrace((StackTraceElement[]) stl.toArray(new StackTraceElement[stl.size()]));
          if (encodingFactory.readInt8() == HAS_MORE) {
            RpcException next = (RpcException) decode(encodingFactory);
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
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    try {
      if (value == null) {
        encodingFactory.writeInt8(IS_NULL);
        return;
      }
      encodingFactory.writeInt8(NOT_NULL);

      RpcException serverException = (RpcException) value;

      encodingFactory.writeStringNullable(serverException.getMessage());

      encodingFactory.writeStringNullable(serverException.getCausedByClassName());

      encodingFactory.writeStringNullable(serverException.getCausedByStackTrace());

      Throwable cause = serverException.getCause();
      if (cause != null) {
        encodingFactory.writeInt8(HAS_CAUSE);

        String causeName = cause.getClass().getName();
        RpcCodec codec = encodingFactory != null ? encodingFactory.getEncodingObject(causeName, TypeCategory.JAVA) : null;
        if (codec != null) {
          encodingFactory.writeInt8(HAS_CODEC);
          encodingFactory.writeObjectNullable(cause);
        } else {
          encodingFactory.writeInt8(NO_CODEC);
          StackTraceElement[] st = cause.getStackTrace();

          encodingFactory.writeInt32(st.length);
          for (StackTraceElement ste : st) {
            encodingFactory.writeStringNullable(ste.getClassName());
            encodingFactory.writeStringNullable(ste.getMethodName());
            encodingFactory.writeStringNullable(ste.getFileName());
            encodingFactory.writeInt32(ste.getLineNumber());
          }
          cause = cause.getCause();
          if (cause != null) {
            encodingFactory.writeInt8(HAS_MORE);
            RpcException next = new RpcException(cause.getClass().getCanonicalName() + ":" + cause.getMessage(), cause);
            encode(next, encodingFactory);
          } else {
            encodingFactory.writeInt8(NO_MORE);
          }
        }
      } else {
        encodingFactory.writeInt8(NO_CAUSE);
      }
    } catch (IOException e) {
      String message = "Could not encode KisRpcServerException - " + e;
      logger.error(message, e);
      throw new RpcException(message);
    }
  }
}
