package com.elminster.easy.rpc.codec.impl;

public class CodecFactoryImpl
implements CodecFactory
{
public CodecRepositoryElement createCodecRepositoryElement(String idlName, String className, KisRpcCodec codec)
{
  if (null == idlName) {
    throw new IllegalArgumentException("Parameter 'idlName' must not be null!");
  }
  if (idlName.isEmpty()) {
    throw new IllegalArgumentException("Parameter 'idlName' must not be an empty string");
  }
  if (null == className) {
    throw new IllegalArgumentException("Parameter 'className' must not be null!");
  }
  if (className.isEmpty()) {
    throw new IllegalArgumentException("Parameter 'className' must not be an empty string");
  }
  if (null == codec) {
    throw new IllegalArgumentException("Parameter 'codec' must not be null!");
  }
  return new CodecRepositoryElementImpl(idlName, className, codec);
}

public KisRpcCodec createDefaultCodec(IDLBasicTypeAccess basicType)
{
  if (null == basicType) {
    throw new IllegalArgumentException("Parameter 'basicType' must not be null!");
  }
  try
  {
    return (KisRpcCodec)basicType.getCodecClass().newInstance();
  }
  catch (InstantiationException|IllegalAccessException e)
  {
    throw new IllegalStateException("Failed to instantiate default codec class", e);
  }
}

public KisRpcEncodingFactory createEncodingFactory()
{
  return new KisRpcEncodingFactoryBase();
}

public KisRpcEncodingFactory createEncodingFactory(CodecRepository repository, String newEncoding)
{
  if (null == repository) {
    throw new IllegalArgumentException("Parameter 'repository' must not be null!");
  }
  if (null == newEncoding) {
    throw new IllegalArgumentException("Parameter 'newEncoding' must not be null!");
  }
  if (newEncoding.isEmpty()) {
    throw new IllegalArgumentException("Parameter 'newEncoding' must not be an empty string!");
  }
  return new KisRpcEncodingFactoryBase(repository, newEncoding);
}

public KisRpcEncodingFactory createEncodingFactory(Collection<CodecRepository> repositories, String newEncoding)
{
  if (null == repositories) {
    throw new IllegalArgumentException("Parameter 'repositories' must not be null!");
  }
  if (null == newEncoding) {
    throw new IllegalArgumentException("Parameter 'newEncoding' must not be null!");
  }
  if (newEncoding.isEmpty()) {
    throw new IllegalArgumentException("Parameter 'newEncoding' must not be null!");
  }
  return new KisRpcEncodingFactoryBase(repositories, newEncoding);
}

public KisRpcCodec createServerExceptionCodec()
{
  return new KisRpcServerExceptionCodec();
}

public <ResultType, ProgressType> KisRpcFunctionCall<ResultType, ProgressType> createFunctionCall(Long uniqueFunctionId)
{
  if (null == uniqueFunctionId) {
    throw new IllegalArgumentException("Parameter 'uniqueFunctionId' must not be null!");
  }
  return new KisRpcFunctionCallImpl(uniqueFunctionId);
}

public <ResultType, ProgressType> KisRpcFunctionCall<ResultType, ProgressType> createFunctionCall()
{
  KisRpcFunctionCall<ResultType, ProgressType> call = new KisRpcFunctionCallImpl();
  call.setConnectionId(Long.valueOf(-1L));
  call.setFunctionId(Long.valueOf(0L));
  return call;
}
}

