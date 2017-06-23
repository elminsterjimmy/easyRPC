package com.elminster.easy.rpc.idl;

import com.elminster.easy.rpc.codec.RpcCodec;

/**
 * The IDL Type.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface IDL {

  /**
   * Get local name. (JAVA Class Name)
   * @return local name
   */
  public String getLocalName();
  
  /**
   * Get remote name.
   * @return remote name
   */
  public String getRemoteName();
  
  /**
   * Get type class.
   * @return type class
   */
  public Class<?> getTypeClass();
  
  /**
   * Get codec class.
   * @return codec class
   */
  public Class<? extends RpcCodec> getCodecClass();
}
