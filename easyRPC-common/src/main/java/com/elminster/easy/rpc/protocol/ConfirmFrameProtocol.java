package com.elminster.easy.rpc.protocol;

import java.io.IOException;

public interface ConfirmFrameProtocol extends Protocol {

  /**
   * Will decode the frame!
   * @param frame
   * @return
   * @throws IOException
   */
  public boolean expact(byte frame) throws IOException;
  
  /**
   * will encode the next frame!
   * @param frame
   * @throws IOException
   */
  public void nextFrame(byte frame) throws IOException;
  
  public byte getFrame();
  
  public static enum Frame {
    FRAME_NONE((byte)0),
    FRAME_VERSION((byte)1),
    FRAME_HEADER((byte)2),
    FRAME_REQUEST((byte)3),
    FRAME_RESPONSE((byte)4),
    FRAME_FAIL((byte)0xF0),
    FRAME_EXCEPTION((byte)0xFF);
    
    private byte frame;
    
    Frame(byte frame) {
      this.frame = frame;
    }
    
    public byte getFrame() {
      return frame;
    }
  }
}
