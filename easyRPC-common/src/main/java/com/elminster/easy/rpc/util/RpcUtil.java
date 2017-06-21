package com.elminster.easy.rpc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RpcUtil {

  public void writeByte(OutputStream paramOutputStream, byte paramByte) throws IOException;

  public byte readByte(InputStream paramInputStream) throws IOException;

  public void writeIntBigEndian(OutputStream paramOutputStream, int paramInt) throws IOException;

  public int readIntBigEndian(InputStream paramInputStream) throws IOException;

  public void writeLongBigEndian(OutputStream paramOutputStream, long paramLong) throws IOException;

  public long readLongBigEndian(InputStream paramInputStream) throws IOException;

  public void readn(InputStream paramInputStream, byte[] paramArrayOfByte, int paramInt1, int paramInt2) throws IOException;

  public void writeStringAsciiNullable(OutputStream paramOutputStream, String paramString) throws IOException;

  public void writeStringUTF8Nullable(OutputStream paramOutputStream, String paramString) throws IOException;

  public void writeStringAscii(OutputStream paramOutputStream, String paramString) throws IOException;

  public void writeStringUTF8(OutputStream paramOutputStream, String paramString) throws IOException;

  public String readStringAsciiNullable(InputStream paramInputStream) throws IOException;

  public String readStringUTF8Nullable(InputStream paramInputStream) throws IOException;

  public String readStringAscii(InputStream paramInputStream) throws IOException;

  public String readStringUTF8(InputStream paramInputStream) throws IOException;

  public boolean compareVersions(String paramString1, String paramString2);
}
