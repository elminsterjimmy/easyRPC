package com.elminster.easy.rpc.codec.impl;

import com.elminster.easy.rpc.codec.CodecRepositoryElement;
import com.elminster.easy.rpc.codec.RpcCodec;

/**
 * The Codec Repository Element.
 * 
 * @author jinggu
 * @version 1.0
 */
public class CodecRepositoryElementImpl implements CodecRepositoryElement {

  /** the IDL name. */
  private final String idlName;
  /** the class name. */
  private final String className;
  /** the codec. */
  private final RpcCodec codec;

  /**
   * Constructor.
   * @param idlName the IDL name
   * @param className the class name
   * @param codec the codec
   */
  public CodecRepositoryElementImpl(final String idlName, final String className, final RpcCodec codec) {
    this.idlName = idlName;
    this.className = className;
    this.codec = codec;
  }

  /**
   * {@inheritDoc}
   */
  public String getIdlName() {
    return idlName;
  }

  /**
   * {@inheritDoc}
   */
  public String getClassName() {
    return className;
  }

  /**
   * {@inheritDoc}
   */
  public RpcCodec getCodec() {
    return codec;
  }
}
