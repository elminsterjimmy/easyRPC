package com.elminster.easy.rpc.codec;

import java.util.List;

/**
 * The Codec Repository.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface CodecRepository {

  /**
   * Get the Codec Repository Element List.
   * @return the Codec Repository Element List
   */
  public List<CodecRepositoryElement> getCodecRepositoryElementList();
}
