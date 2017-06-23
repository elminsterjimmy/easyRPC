package com.elminster.easy.rpc.codec;

/**
 * The Codec Repository Element.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface CodecRepositoryElement {

  /**
   * Get the IDL name.
   * @return IDL name
   */
  public String getIdlName();
  
  /**
   * Get the class name.
   * @return the class name
   */
  public String getClassName();
  
  /**
   * Get the codec.
   * @return the codec
   */
  public RpcCodec getCodec();
}
