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
    FRAME_OK((byte)1),
    FRAME_VERSION((byte)10),
    FRAME_HEADER((byte)11),
    FRAME_REQUEST((byte)12),
    FRAME_RESPONSE((byte)13),
    FRAME_ASYNC_RESPONSE((byte)14),
    FRAME_GRACE_EXIT((byte)0xA0),
    FRAME_MISREAD((byte)80),
    FRAME_CANCELLED((byte)90),
    FRAME_TIMEOUT((byte)91),
    FRAME_UNAVAILABLE((byte)92),
    FRAME_ENCODE_ERROR((byte)0),
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
