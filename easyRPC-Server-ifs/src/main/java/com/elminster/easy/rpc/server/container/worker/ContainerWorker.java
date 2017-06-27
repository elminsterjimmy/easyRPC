package com.elminster.easy.rpc.server.container.worker;

/**
 * The Container Worker.
 * 
 * @author jinggu
 * @version 1.0
 */
public interface ContainerWorker {

  public void doAccept();
  
  public void doConnect();
  
  public void doRead();
  
  public void doWrite();
  
  public void doInvaild();
}
