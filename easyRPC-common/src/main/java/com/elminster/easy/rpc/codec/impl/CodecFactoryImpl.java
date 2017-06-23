package com.elminster.easy.rpc.codec.impl;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.codec.CodecFactory;
import com.elminster.easy.rpc.codec.CodecRepository;
import com.elminster.easy.rpc.codec.CodecRepositoryElement;
import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.idl.IDL;

/**
 * The Codec Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public class CodecFactoryImpl implements CodecFactory {
  
  private static AtomicInteger INC_SER_GEN = new AtomicInteger();

  /**
   * {@inheritDoc}
   */
  @Override
  public CodecRepositoryElement createCodecRepositoryElement(String idlName, String className, RpcCodec codec) {
    Assert.notEmpty(idlName);
    Assert.notEmpty(className);
    Assert.notNull(codec);
    return new CodecRepositoryElementImpl(idlName, className, codec);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcCodec createCodecForIDL(IDL idlType) {
    Assert.notNull(idlType);
    try {
      return idlType.getCodecClass().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new IllegalStateException("Failed to instantiate default codec class", e);
    }
  }

  /**
   * Create a default encoding factory.
   * @return a default encoding factory
   */
  public RpcEncodingFactory createEncodingFactory() {
    return new RpcEncodingFactoryBase(this.toString() + "-" + INC_SER_GEN.incrementAndGet());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcEncodingFactory createEncodingFactory(CodecRepository repository, String encodingFactoryName) {
    Assert.notNull(repository);
    Assert.notEmpty(encodingFactoryName);
    return new RpcEncodingFactoryBase(repository, encodingFactoryName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcEncodingFactory createEncodingFactory(Collection<CodecRepository> repositories, String encodingFactoryName) {
    Assert.notNull(repositories);
    Assert.notEmpty(encodingFactoryName);
    return new RpcEncodingFactoryBase(repositories, encodingFactoryName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcCodec createServerExceptionCodec() {
    return new RpcServerExceptionCodec();
  }
  
//  public <ResultType, ProgressType> RpcFunctionCall<ResultType, ProgressType> createFunctionCall(Long uniqueFunctionId) {
//    if (null == uniqueFunctionId) {
//      throw new IllegalArgumentException("Parameter 'uniqueFunctionId' must not be null!");
//    }
//    return new RpcFunctionCallImpl(uniqueFunctionId);
//  }
//
//  public <ResultType, ProgressType> RpcFunctionCall<ResultType, ProgressType> createFunctionCall() {
//    RpcFunctionCall<ResultType, ProgressType> call = new RpcFunctionCallImpl();
//    call.setConnectionId(Long.valueOf(-1L));
//    call.setFunctionId(Long.valueOf(0L));
//    return call;
//  }
}
