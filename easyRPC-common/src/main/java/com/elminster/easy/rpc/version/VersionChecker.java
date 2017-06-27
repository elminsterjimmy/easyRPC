package com.elminster.easy.rpc.version;

import static com.elminster.common.constants.RegexConstants.REGEX_DOT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionChecker {
  
  private static final Logger logger = LoggerFactory.getLogger(VersionChecker.class);

  /**
   * Compare version A and version B.
   * 
   * @param va
   *          the version A
   * @param vb
   *          the version B
   * @return version compatible?
   */
  public static boolean compatible(String va, String vb) {
    if (logger.isDebugEnabled()) {
      logger.debug("Version A = " + va + "; version B = " + vb);
    }
    if ((va == null) || (vb == null)) {
      return false;
    }
    if (va.equals(vb)) {
      return true;
    }
    String[] arrA = va.split(REGEX_DOT);
    String[] arrB = vb.split(REGEX_DOT);
    int length = Math.min(arrA.length, arrB.length);
    if (length < 2) {
      logger.error("Incorrect version format [" + va + "; " + vb + "]. Required: x.y.*");
      return false;
    }
    return (arrA[0] + arrA[1]).equals(arrB[0] + arrB[1]);
  }
}
