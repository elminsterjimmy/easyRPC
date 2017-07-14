package com.elminster.easy.rpc.call;

public enum Status {

  CREATED,
  UNPROCCESSED,
  PROCESSING,
  CANCELLED,
  PROCESSED,
  EXCEPTION;

  public static boolean isDone(Status status) {
    return PROCESSED == status || EXCEPTION == status;
  }
  
}
