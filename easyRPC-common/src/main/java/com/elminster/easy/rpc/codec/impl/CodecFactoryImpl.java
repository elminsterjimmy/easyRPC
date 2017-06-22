package com.elminster.easy.rpc.codec.impl;

import java.util.Collection;

import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.codec.CodecFactory;
import com.elminster.easy.rpc.codec.CodecRepository;
import com.elminster.easy.rpc.codec.CodecRepositoryElement;
import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.idl.IDLType;

public class CodecFactoryImpl implements CodecFactory {

  public CodecRepositoryElement createCodecRepositoryElement(String idlName, String className, RpcCodec codec) {
    Assert.notEmpty(idlName);
    Assert.notEmpty(className);
    Assert.notNull(codec);
    return new CodecRepositoryElementImpl(idlName, className, codec);
  }

  public RpcCodec createDefaultCodec(IDLType idlType) {
    Assert.notNull(idlType);
    try {
      return idlType.getCodecClass().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new IllegalStateException("Failed to instantiate default codec class", e);
    }
  }

  public RpcEncodingFactory createEncodingFactory() {
    return new RpcEncodingFactoryBase();
  }

  public RpcEncodingFactory createEncodingFactory(CodecRepository repository, String encodingName) {
    Assert.notNull(repository);
    Assert.notEmpty(encodingName);
    return new RpcEncodingFactoryBase(repository, encodingName);
  }

  public RpcEncodingFactory createEncodingFactory(Collection<CodecRepository> repositories, String encodingName) {
    Assert.notNull(repositories);
    Assert.notEmpty(encodingName);
    return new RpcEncodingFactoryBase(repositories, encodingName);
  }

  public RpcCodec createServerExceptionCodec() {
    // TODO
    return null;
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
