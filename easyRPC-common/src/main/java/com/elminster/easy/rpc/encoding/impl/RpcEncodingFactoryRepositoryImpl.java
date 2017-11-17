package com.elminster.easy.rpc.encoding.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.elminster.common.util.Assert;
import com.elminster.easy.rpc.codec.Codec;
import com.elminster.easy.rpc.encoding.RpcEncodingFactory;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryNotFoundException;
import com.elminster.easy.rpc.encoding.RpcEncodingFactoryRepository;

/**
 * The RPC Encoding Factory Repository.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcEncodingFactoryRepositoryImpl implements RpcEncodingFactoryRepository {
  
  /** the encoding factories. */
  protected Map<String, RpcEncodingFactory> encodingFactories = new ConcurrentHashMap<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcEncodingFactory getRpcEncodingFactory(String encodingFactoryName, Codec codec) throws RpcEncodingFactoryNotFoundException {
    RpcEncodingFactory rtn = null;
    RpcEncodingFactory factory = encodingFactories.get(encodingFactoryName);
    if (null != factory) {
      rtn = factory.cloneEncodingFactory();
      rtn.setCodec(codec);
    } else {
      throw new RpcEncodingFactoryNotFoundException(encodingFactoryName);
    }
    return rtn;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addRpcEncodingFactory(RpcEncodingFactory encodingFactory) {
    Assert.notNull(encodingFactory);
    encodingFactories.put(encodingFactory.getName(), encodingFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeRpcEncodingFactory(String encodingFactoryName) {
    encodingFactories.remove(encodingFactoryName);
  }

}
