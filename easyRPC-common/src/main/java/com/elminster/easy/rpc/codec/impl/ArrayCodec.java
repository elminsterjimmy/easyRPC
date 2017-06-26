package com.elminster.easy.rpc.codec.impl;

import java.lang.reflect.Array;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.util.ReflectUtil;
import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.idl.IDL;
import com.elminster.easy.rpc.idl.impl.IDLBasicTypes;

/**
 * The array codec.
 * 
 * @author jinggu
 * @version 1.0
 */
public class ArrayCodec implements RpcCodec {
  
  /** the logger. */
  private static Logger logger = LoggerFactory.getLogger(ArrayCodec.class);

  /**
   * {@inheritDoc}
   */
  public Object decode(final RpcEncodingFactory encodingFactory) throws RpcException {
    String remoteTypeName = null;
    String arrayTypeName = null;
    try {
      int arraySize = ((Integer) encodingFactory.readObjectNullable()).intValue();
      remoteTypeName = (String) encodingFactory.readObjectNullable();
      arrayTypeName = encodingFactory.getClassNameForRemoteName(remoteTypeName);
      if (arrayTypeName == null) {
        throw new Exception("No class defined for " + remoteTypeName);
      }
      RpcCodec typeEncoder = encodingFactory.getEncodingObject(arrayTypeName, TypeCategory.JAVA);
      if (typeEncoder == null) {
        throw new Exception("No Codec found for class " + arrayTypeName);
      }
      IDL bt = IDLBasicTypes.getByRemoteName(remoteTypeName);
      Class<?> typeClass = bt != null ? bt.getTypeClass() : ReflectUtil.forName(arrayTypeName);

      Object array = Array.newInstance(typeClass, arraySize);
      Object[] decoded = new Object[arraySize];
      for (int i = 0; i < arraySize; i++) {
        boolean notNull = encodingFactory.readIsNotNull();
        Object entry = null;
        if (notNull) {
          entry = typeEncoder.decode(encodingFactory);
        }
        decoded[i] = entry;
      }
      System.arraycopy(decoded, 0, array, 0, arraySize);
      return array;
    } catch (RpcException k) {
      throw k;
    } catch (Exception e) {
      String typeName = arrayTypeName != null ? arrayTypeName : remoteTypeName;
      String message = "Could not decode array of " + typeName + "[] - " + e;
      logger.error(message, e);
      throw new RpcException(message);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void encode(final Object value, final RpcEncodingFactory encodingFactory) throws RpcException {
    String arrayTypeName = null;
    try {
      Class<?> arrayClass = value.getClass();
      if (!arrayClass.isArray()) {
        throw new Exception("Object is not an array");
      }
      Class<?> componentClass = arrayClass.getComponentType();
      arrayTypeName = componentClass.getName();
      String remoteTypeName = encodingFactory.getRemoteNameForClassName(arrayTypeName);
      
      if (remoteTypeName == null) {
        throw new Exception("No remote name defined for class " + componentClass);
      }
      int arraySize = Array.getLength(value);
      encodingFactory.writeObjectNullable(Integer.valueOf(arraySize));

      encodingFactory.writeObjectNullable(remoteTypeName);

      RpcCodec componentEncoder = encodingFactory.getEncodingObject(componentClass);

      for (Object obj : (Object[]) value) {
        encodingFactory.writeIsNotNull(null != obj);
        if (null != obj) {
          // auto boxing and auto unboxing
          componentEncoder.encode(obj, encodingFactory);
        }
      }
    } catch (RpcException e) {
      throw e;
    } catch (Exception e) {
      String message = "Could not encode array of " + arrayTypeName + "[] - " + e;
      logger.error(message, e);
      throw new RpcException(message);
    }
  }
}
