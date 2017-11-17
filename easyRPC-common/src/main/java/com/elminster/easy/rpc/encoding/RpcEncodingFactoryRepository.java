package com.elminster.easy.rpc.encoding;

import com.elminster.easy.rpc.codec.Codec;

/**
 * The RPC Encoding Factory Repository.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RpcEncodingFactoryRepository {

  /**
   * Get the RPC encoding factory by encoding factory name.
   * 
   * 
   * @param encodingFactoryName
   *          the encoding factory name
   * @param codec
   *          the codec
   * @return the RPC encoding factory with specified name
   * 
   * @throws RpcEncodingFactoryNotFoundException
   *           if the encoding factory with specified encoding factory name is not found.
   */
  public RpcEncodingFactory getRpcEncodingFactory(final String encodingFactoryName, final Codec codec) throws RpcEncodingFactoryNotFoundException;

  /**
   * Add a RPC encoding factory.
   * 
   * @param encodingFactory
   *          the encoding factory
   */
  public void addRpcEncodingFactory(final RpcEncodingFactory encodingFactory);

  /**
   * Remove a RPC encoding factory by encoding factory name.
   * 
   * @param encodingFactoryName
   *          the encoding factory name
   */
  public void removeRpcEncodingFactory(final String encodingFactoryName);
}
