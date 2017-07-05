package com.elminster.easy.rpc.server.container.worker.impl;

public enum WorkerJobId {

  BIO_LISTEN_WORKER(10001),
  BIO_CONNECTION(20001),
  NIO_LISTEN_WORKER(50001),
  NIO_READ_WORKER(60001),
  NIO_CONNECTION(90001)
  ;
  
  WorkerJobId(int jobId) {
    this.jobId = jobId;
  }
  
  private int jobId;
  
  public int getJobId() {
    return jobId;
  }
}
