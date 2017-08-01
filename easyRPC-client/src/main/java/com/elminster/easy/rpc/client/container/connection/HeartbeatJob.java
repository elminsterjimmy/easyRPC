package com.elminster.easy.rpc.client.container.connection;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elminster.common.thread.IJobMonitor;
import com.elminster.common.thread.Job;
import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol;
import com.elminster.easy.rpc.protocol.ConfirmFrameProtocol.Frame;

public class HeartbeatJob extends Job {

  private static final Logger logger = LoggerFactory.getLogger(HeartbeatJob.class);
  private Lock lock;
  private ConfirmFrameProtocol confirmFrameProtocol;
  
  public HeartbeatJob(String uuid, Lock lock, ConfirmFrameProtocol confirmFrameProtocol) {
    super(UUID.fromString(uuid).getMostSignificantBits(), "Heartbeat - " + uuid);
    this.lock = lock;
    this.confirmFrameProtocol = confirmFrameProtocol;
  }

  @Override
  protected JobStatus doWork(IJobMonitor monitor) throws Throwable {
    while (!Thread.currentThread().isInterrupted() && !monitor.isCancelled()) {
      try {
        lock.lock();
        confirmFrameProtocol.nextFrame(Frame.FRAME_HEARTBEAT.getFrame());
      } catch (IOException ioe) {
        logger.warn("heartbeat failed caused by IOException: {}", ioe);
        cancel();
      } finally {
        lock.unlock();
      }
    }
    return monitor.done();
  }
}