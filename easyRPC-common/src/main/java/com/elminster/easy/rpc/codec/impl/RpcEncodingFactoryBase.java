package com.elminster.easy.rpc.codec.impl;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.CodecRepository;
import com.elminster.easy.rpc.codec.CodecRepositoryElement;
import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.codec.RpcEncodingFactory;
import com.elminster.easy.rpc.exception.RpcException;
import com.elminster.easy.rpc.idl.impl.IDLBasicTypes;

/**
 * The Base RPC Encoding Factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcEncodingFactoryBase extends RpcEncodingFactoryImpl {

  /** the logger. */
  private static final Logger logger = LoggerFactory.getLogger(RpcEncodingFactoryBase.class);

  /** the default array codec. */
  private ArrayCodec theArrayCodec = new ArrayCodec();

  public RpcEncodingFactoryBase(final String encodingName) {
    super(encodingName);
    addBaseCodecs();
  }

  public RpcEncodingFactoryBase(final CodecRepository repository, final String encodingName) {
    super(encodingName);
    addBaseCodecs();
    addCodecRepository(repository);
  }

  public RpcEncodingFactoryBase(final Collection<CodecRepository> repositories, final String encodingName) {
    super(encodingName);
    addBaseCodecs();
    for (CodecRepository repository : repositories) {
      addCodecRepository(repository);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addCodecRepository(final CodecRepository repository) {
    for (CodecRepositoryElement el : repository.getCodecRepositoryElementList()) {
      addEncodingInstance(el.getClassName(), el.getCodec(), el.getIdlName());
    }
  }

  /**
   * Add all base codes.
   */
  private void addBaseCodecs() {
    for (IDLBasicTypes bt : IDLBasicTypes.values()) {
      addEncodingClass(bt.getTypeClass(), bt.getCodecClass(), bt.getRemoteName());
    }
    addEncodingClass(RpcException.class.getCanonicalName(), RpcServerExceptionCodec.class, RpcException.class.getCanonicalName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected RpcCodec getDefaultArrayCodec() {
    return this.theArrayCodec;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected RpcEncodingFactory clone() {
    RpcEncodingFactoryBase cloned = new RpcEncodingFactoryBase(this.getEncodingName());
    cloned.classNameToRemoteTypeNameMap = this.classNameToRemoteTypeNameMap;
    cloned.encodingClassMap = this.encodingClassMap;
    cloned.encodingInstanceMap = this.encodingInstanceMap;
    cloned.remoteTypeNameToClassNameMap = this.remoteTypeNameToClassNameMap;
    cloned.theArrayCodec = this.theArrayCodec;
    return cloned;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RpcEncodingFactory cloneEncodingFactory() {
    return this.clone();
  }
  
}
