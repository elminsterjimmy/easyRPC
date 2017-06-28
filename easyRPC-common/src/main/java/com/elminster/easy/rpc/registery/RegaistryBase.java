package com.elminster.easy.rpc.registery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.elminster.easy.rpc.exception.ObjectInstantiationExcption;

/**
 * The Registry Template.
 * 
 * @author jinggu
 * @version 1.0
 * @param <T> T
 */
abstract public class RegaistryBase<T> implements Registrable<T> {

  /** the cache. */
  protected Map<String, T> cache = new ConcurrentHashMap<>();
  
  /**
   * find the object from cache, if not found instance it.
   * @param key the key
   * @return the object found
   */
  protected T findObject(String key) throws ObjectInstantiationExcption {
    T t = cache.get(key);
    if (null == t) {
      t = instaceObject(key);
      if (null != t) {
        cache.put(key, t);
      }
    }
    return t;
  }

  /**
   * Instance the object.
   * @param key the key
   * @return the instanced object
   */
  abstract protected T instaceObject(String key) throws ObjectInstantiationExcption;
}
