package com.elminster.easy.rpc.codec;

import java.util.Collection;

import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.idl.IDL;

/**
 * The Codec Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface CodecFactory extends Cloneable {

  /**
   * Create a codec repository element.
   * @param idlName the IDL name
   * @param className the class name
   * @param codec the codec
   * @return the codec repository element
   */
  public CodecRepositoryElement createCodecRepositoryElement(String idlName, String className, RpcCodec codec);

  /**
   * Create the codec for the IDL.
   * @param idl the IDL
   * @return the corresponded codec for the IDL.
   */
  public RpcCodec createCodecForIDL(IDL idl);

  /**
   * Create the Server Exception Codec.
   * @return the Server Exception Codec
   */
  public RpcCodec createServerExceptionCodec();

  /**
   * Create the encoding factory with the codec repository.
   * @param repository the codec repository
   * @param encodingFactoryName the encoding factory name
   * @return the encoding factory
   */
  public RpcEncodingFactory createEncodingFactory(CodecRepository repository, String encodingFactoryName);

  /**
   * Create the encoding factory with the codec repositories.
   * @param repositories the codec repositories
   * @param encodingFactoryName the encoding factory name
   * @return the encoding factory
   */
  public RpcEncodingFactory createEncodingFactory(Collection<CodecRepository> repositories, String encodingFactoryName);
}
