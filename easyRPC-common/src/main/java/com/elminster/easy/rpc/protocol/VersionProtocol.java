package com.elminster.easy.rpc.protocol;

/**
 * The version protocol.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface VersionProtocol extends Protocol {

  public void setVersion(String version);
  
  public String getVersion();
}
