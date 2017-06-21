package com.elminster.easy.rpc.codec.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.logging.Logger;

import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.idl.IDLTypes;

public class ArrayCodec implements RpcCodec {
  
  private static Logger log = LoggerFactory.getLogger(ArrayCodec.class);

  public Object decode(InputStream iStream, RpcEncodingFactory encodingFactory) throws RpcException {
    String remoteTypeName = null;
    String arrayTypeName = null;
    try {
      int arraySize = ((Integer) encodingFactory.readObjectNullable(iStream)).intValue();

      remoteTypeName = (String) encodingFactory.readObjectNullable(iStream);

      arrayTypeName = encodingFactory.getClassNameForRemote(remoteTypeName);
      if (arrayTypeName == null) {
        throw new Exception("No class defined for " + remoteTypeName);
      }
      RpcCodec typeEncoder = encodingFactory.getEncodingObject(arrayTypeName, 1);
      if (typeEncoder == null) {
        throw new Exception("No Codec found for class " + arrayTypeName);
      }
      IDLTypes bt = IDLTypes.getByRemoteName(remoteTypeName);
      Class<?> typeClass = bt != null ? bt.getTypeClass() : Class.forName(arrayTypeName);

      Object array = null;
      boolean isPrimitive = typeClass.isPrimitive();
      if (isPrimitive) {
        array = typeEncoder.convertArray(Array.newInstance(typeClass, arraySize));
      } else {
        array = Array.newInstance(typeClass, arraySize);
      }
      Object[] decoded = new Object[arraySize];
      for (int i = 0; i < arraySize; i++) {
        boolean valid = isPrimitive;
        if (!isPrimitive) {
          valid = encodingFactory.readIsNotNull(iStream);
        }
        Object curArrayEntry = null;
        if (valid) {
          curArrayEntry = typeEncoder.decode(iStream, encodingFactory);
        }
        decoded[i] = curArrayEntry;
      }
      System.arraycopy(decoded, 0, array, 0, arraySize);
      return isPrimitive ? typeEncoder.convertArray(array) : array;
    } catch (RpcException k) {
      throw k;
    } catch (Exception e) {
      String typeName = arrayTypeName != null ? arrayTypeName : remoteTypeName;
      String message = "Could not decode array of " + typeName + "[] - " + e;
      log.error(message, e);
      throw new RpcException(message);
    }
  }

  public void encode(OutputStream oStream, Object value, RpcEncodingFactory encodingFactory) throws RpcException {
    String arrayTypeName = null;
    try {
      Class<?> arrayClass = value.getClass();
      if (!arrayClass.isArray()) {
        throw new Exception("Object is not an array");
      }
      Class<?> componentClass = arrayClass.getComponentType();
      arrayTypeName = componentClass.getName();

      String remoteTypeName = encodingFactory.getRemoteForClassName(arrayTypeName);
      if (remoteTypeName == null) {
        throw new Exception("No remote name defined for class " + componentClass);
      }
      int arraySize = Array.getLength(value);
      encodingFactory.writeObjectNullable(oStream, Integer.valueOf(arraySize));

      encodingFactory.writeObjectNullable(oStream, remoteTypeName);

      RpcCodec componentEncoder = encodingFactory.getEncodingObject(componentClass);

      Object[] array = null;
      boolean isPrimitive = componentClass.isPrimitive();
      if (isPrimitive) {
        array = (Object[]) componentEncoder.convertArray(value);
      } else {
        array = (Object[]) value;
      }
      for (Object obj : array) {
        if (!isPrimitive) {
          encodingFactory.writeIsNotNull(oStream, obj != null);
        }
        if (obj != null) {
          componentEncoder.encode(oStream, obj, encodingFactory);
        }
      }
    } catch (RpcException k) {
      throw k;
    } catch (Exception e) {
      String message = "Could not encode array of " + arrayTypeName + "[] - " + e;
      log.error(message, e);
      throw new RpcException(message);
    }
  }

  public Object decode(InputStream iStream, Object codecData, RpcEncodingFactory encodingFactory) throws RpcException {
    return decode(iStream, encodingFactory);
  }

  public Object convertArray(Object value) {
    return value;
  }
}
