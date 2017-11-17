package com.elminster.easy.rpc.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation indicate the RPC service.
 * 
 * @author jinggu
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Rpc {

  /**
   * The RPC service name.
   * 
   * @return the RPC service name
   */
  public String value();
}
