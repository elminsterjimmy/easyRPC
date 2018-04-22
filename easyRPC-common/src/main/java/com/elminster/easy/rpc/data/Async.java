package com.elminster.easy.rpc.data;

public enum Async {
  SYNC((byte) 0), ASYNC((byte) 1);

  private final byte value;

  Async(byte value) {
    this.value = value;
  }
  
  public byte value() {
    return value;
  }
  
  public static Async toAsync(byte value) {
    for (Async flag : values()) {
      if (flag.value() == value) {
        return flag;
      }
    }
    return null;
  }
}
