package com.elminster.easy.rpc.server.container.worker.impl;

/**
 * An enum of workers' job id.
 * 
 * @author jinggu
 * @version 1.0
 */
public enum WorkerJobId {

  BIO_LISTEN_WORKER(0x10000),
  BIO_CONNECTION(0x20000),
  NIO_LISTEN_WORKER(0x50000),
  NIO_READ_WORKER(0x60000),
  NIO_CONNECTION(0x90000),
  PROCESS_JOB(0x95000),
  PROCESS_WORKER(0x100000)
  ;
  
  WorkerJobId(int jobId) {
    this.jobId = jobId;
  }
  
  private int jobId;
  
  public int getJobId() {
    return jobId;
  }
}
