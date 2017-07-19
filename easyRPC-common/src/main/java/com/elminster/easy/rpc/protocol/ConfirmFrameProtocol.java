package com.elminster.easy.rpc.protocol;

import java.io.IOException;

public interface ConfirmFrameProtocol extends Protocol {

  /**
   * Will decode the frame!
   * 
   * @param frame
   * @return
   * @throws IOException
   */
  public boolean expact(byte frame) throws IOException;

  /**
   * will encode the next frame!
   * 
   * @param frame
   * @throws IOException
   */
  public void nextFrame(byte frame) throws IOException;

  public byte getFrame();
  
  public static enum Frame {
    // @formatter:off
    FRAME_NONE((byte)0),
    FRAME_OK((byte)1),
    FRAME_SHAKEHAND((byte)9),
    FRAME_VERSION((byte)10),
    FRAME_HEADER((byte)11),
    FRAME_REQUEST((byte)12),
    FRAME_RESPONSE((byte)13),
    FRAME_ASYNC_REQUEST((byte)14),
    FRAME_ASYNC_RESPONSE((byte)15),
    FRAME_VERSION_INCOMPATIBLE((byte)0x50),
    FRAME_MISREAD((byte)80),
    FRAME_CANCELLED((byte)90),
    FRAME_TIMEOUT((byte)91),
    FRAME_UNAVAILABLE((byte)92),
    FRAME_ENCODE_ERROR((byte)0),
    FRAME_GRACE_EXIT((byte)0xA0),
    FRAME_UNKOWN((byte)0xEE),
    FRAME_EXCEPTION((byte)0xFF);
    // @formatter:on
    private byte frame;

    Frame(byte frame) {
      this.frame = frame;
    }

    public byte getFrame() {
      return frame;
    }

    public static Frame getFrame(byte b) {
      for (Frame frame : values()) {
        if (frame.getFrame() == b) {
          return frame;
        }
      }
      return Frame.FRAME_UNKOWN;
    }
  }
}
