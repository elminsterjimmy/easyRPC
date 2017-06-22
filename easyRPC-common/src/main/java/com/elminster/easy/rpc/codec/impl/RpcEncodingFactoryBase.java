package com.elminster.easy.rpc.codec.impl;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.easy.rpc.codec.CodecRepository;
import com.elminster.easy.rpc.codec.CodecRepositoryElement;
import com.elminster.easy.rpc.codec.RpcCodec;
import com.elminster.easy.rpc.idl.impl.IDLBasicTypes;

public class RpcEncodingFactoryBase extends RpcEncodingFactoryImpl {

  private static Logger log = LoggerFactory.getLogger(RpcEncodingFactoryBase.class);

  private ArrayCodec theArrayCodec = new ArrayCodec();

  public RpcEncodingFactoryBase() {
    addBaseCodecs();
  }

  public RpcEncodingFactoryBase(CodecRepository repository, String encodingName) {
    this.encoding = encodingName;
    addBaseCodecs();
    addCodecRepository(repository);
  }

  public RpcEncodingFactoryBase(Collection<CodecRepository> repositories, String encodingName) {
    this.encoding = encodingName;
    addBaseCodecs();
    for (CodecRepository repository : repositories) {
      addCodecRepository(repository);
    }
  }

  public void addCodecRepository(CodecRepository repository) {
    for (CodecRepositoryElement el : repository.getDataObjectClassList()) {
      addEncodingInstance(el.getClassName(), el.getCodec(), el.getIdlName());
    }
  }

  private void addBaseCodecs() {
    for (IDLBasicTypes bt : IDLBasicTypes.values()) {
      if (log.isDebugEnabled()) {
        log.debug("Add IDL type:" + bt);
      }
      addEncodingClass(bt.getTypeClass(), bt.getCodecClass(), bt.getRemoteName());
    }
    // addEncodingInstance(KisRpcFunctionCallImpl.class, new KisRpcFunctionCallCodec(), "KisRpcFC");
  }

  protected RpcCodec getDefaultArrayCodec() {
    return this.theArrayCodec;
  }
}
