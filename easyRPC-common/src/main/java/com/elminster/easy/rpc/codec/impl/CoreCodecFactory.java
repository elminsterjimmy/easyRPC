package com.elminster.easy.rpc.codec.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.elminster.easy.rpc.codec.CoreCodec;
import com.elminster.easy.rpc.util.ByteBufferIoImpl;
import com.elminster.easy.rpc.util.IoUtil;
import com.elminster.easy.rpc.util.NioChannelUtil;
import com.elminster.easy.rpc.util.StreamIOUitlImpl;

/**
 * The core codec factory.
 * 
 * @author jinggu
 * @version 1.0
 */
public class CoreCodecFactory {
  
  public static final CoreCodecFactory INSTANCE = new CoreCodecFactory();
  
  private CoreCodecFactory() {}

  public CoreCodec getCoreCodec(InputStream in, OutputStream out) {
    IoUtil ioUtil = new StreamIOUitlImpl(in, out);
    return new CoreCodecImpl(ioUtil);
  }

  public CoreCodec getCoreCodec(ByteBuffer byteBuffer) {
    IoUtil ioUtil = new ByteBufferIoImpl(byteBuffer);
    return new CoreCodecImpl(ioUtil);
  }
  
  public CoreCodec getCoreCodec(SocketChannel socketChannel) {
    IoUtil ioUtil = new NioChannelUtil(socketChannel);
    return new CoreCodecImpl(ioUtil);
  }
}
