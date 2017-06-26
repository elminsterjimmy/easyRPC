package com.elminster.easy.rpc.util;

import java.io.IOException;

public interface IoUtil {

  public void write(byte[] bytes, int off, int len) throws IOException;

  public int read(byte[] bytes, int off, int len) throws IOException;
}
