package com.elminster.easy.rpc.server.context.impl;

import com.elminster.common.threadpool.ThreadPoolConfiguration;
import com.elminster.easy.rpc.connection.impl.NIOSocketFactoryImpl;
import com.elminster.easy.rpc.connection.impl.StreamSocketFactoryImpl;
import com.elminster.easy.rpc.context.RpcContext;
import com.elminster.easy.rpc.server.container.impl.BioContainer;
import com.elminster.easy.rpc.server.container.impl.NioContainer;

/**
 * The RPC server context.
 * 
 * @author jinggu
 * @version 1.0
 */
public class RpcServerContext implements RpcContext {

  private String serverContainerClassName;
  private String socketFactoryClassName;
  private Integer clientTimeout;
  private Boolean clientTcpNoDelay;
  private ThreadPoolConfiguration workerThreadPoolConfiguration;
  private int readerWorkerCount = 8;
  private int processingQueueSize = 200;
  private ThreadPoolConfiguration processingThreadPoolConfiguration;
  
  public String getServerContainerClassName() {
    return serverContainerClassName;
  }
  public void setServerContainerClassName(String serverContainerClassName) {
    this.serverContainerClassName = serverContainerClassName;
  }
  public String getSocketFactoryClassName() {
    return socketFactoryClassName;
  }
  public void setSocketFactoryClassName(String socketFactoryClassName) {
    this.socketFactoryClassName = socketFactoryClassName;
  }
  public Integer getClientTimeout() {
    return clientTimeout;
  }
  public void setClientTimeout(Integer clientTimeout) {
    this.clientTimeout = clientTimeout;
  }
  public Boolean getClientTcpNoDelay() {
    return clientTcpNoDelay;
  }
  public void setClientTcpNoDelay(Boolean clientTcpNoDelay) {
    this.clientTcpNoDelay = clientTcpNoDelay;
  }
  @Override
  public String getClientContainerClassName() {
    return null;
  }
  @Override
  public ThreadPoolConfiguration getWorkerThreadPoolConfiguration() {
    return workerThreadPoolConfiguration;
  }
  public void setWorkerThreadPoolConfiguration(ThreadPoolConfiguration workerThreadPoolConfiguration) {
    this.workerThreadPoolConfiguration = workerThreadPoolConfiguration;
  }
  public int getReaderWorkerCount() {
    return readerWorkerCount;
  }
  public void setReaderWorkerCount(Integer readerWorkerCount) {
    this.readerWorkerCount = readerWorkerCount;
  }
  @Override
  public int getProcessorQueueSize() {
    return this.processingQueueSize;
  }
  public int getProcessingQueueSize() {
    return processingQueueSize;
  }
  public void setProcessingQueueSize(int processingQueueSize) {
    this.processingQueueSize = processingQueueSize;
  }
  public void setReaderWorkerCount(int readerWorkerCount) {
    this.readerWorkerCount = readerWorkerCount;
  }
  public void setProcessingThreadPoolConfiguration(ThreadPoolConfiguration processingThreadPoolConfiguration) {
    this.processingThreadPoolConfiguration = processingThreadPoolConfiguration;
  }
  @Override
  public ThreadPoolConfiguration getProcessingThreadPoolConfiguration() {
    return processingThreadPoolConfiguration;
  }
  public static RpcContext createBioServerContext() {
    RpcServerContext context = new RpcServerContext();
    context.setServerContainerClassName(BioContainer.class.getName());
    context.setSocketFactoryClassName(StreamSocketFactoryImpl.class.getName());
    ThreadPoolConfiguration threadpoolConfig = new ThreadPoolConfiguration();
    threadpoolConfig.setProperty(ThreadPoolConfiguration.CORE_POOL_SIZE.getKey(), 100);
    threadpoolConfig.setProperty(ThreadPoolConfiguration.MAX_POOL_SIZE.getKey(), 100);
    threadpoolConfig.setProperty(ThreadPoolConfiguration.BLOCKING_QUEUE_SIZE.getKey(), 200);
    context.setWorkerThreadPoolConfiguration(threadpoolConfig);
    context.setReaderWorkerCount(8);
    context.setProcessingQueueSize(200);
    ThreadPoolConfiguration processingThreadPoolConfig = new ThreadPoolConfiguration();
    processingThreadPoolConfig.setProperty(ThreadPoolConfiguration.CORE_POOL_SIZE.getKey(), 10);
    processingThreadPoolConfig.setProperty(ThreadPoolConfiguration.MAX_POOL_SIZE.getKey(), 100);
    processingThreadPoolConfig.setProperty(ThreadPoolConfiguration.BLOCKING_QUEUE_SIZE.getKey(), 200);
    context.setProcessingThreadPoolConfiguration(processingThreadPoolConfig);
    return context;
  }
  public static RpcContext createNioServerContext() {
    RpcServerContext context = new RpcServerContext();
    context.setServerContainerClassName(NioContainer.class.getName());
    context.setSocketFactoryClassName(NIOSocketFactoryImpl.class.getName());
    context.setProcessingQueueSize(200);
    ThreadPoolConfiguration threadpoolConfig = new ThreadPoolConfiguration();
    threadpoolConfig.setProperty(ThreadPoolConfiguration.CORE_POOL_SIZE.getKey(), 100);
    threadpoolConfig.setProperty(ThreadPoolConfiguration.MAX_POOL_SIZE.getKey(), 100);
    threadpoolConfig.setProperty(ThreadPoolConfiguration.BLOCKING_QUEUE_SIZE.getKey(), 200);
    context.setWorkerThreadPoolConfiguration(threadpoolConfig);
    context.setReaderWorkerCount(5);
    ThreadPoolConfiguration processingThreadPoolConfig = new ThreadPoolConfiguration();
    processingThreadPoolConfig.setProperty(ThreadPoolConfiguration.CORE_POOL_SIZE.getKey(), 10);
    processingThreadPoolConfig.setProperty(ThreadPoolConfiguration.MAX_POOL_SIZE.getKey(), 100);
    processingThreadPoolConfig.setProperty(ThreadPoolConfiguration.BLOCKING_QUEUE_SIZE.getKey(), 200);
    context.setProcessingThreadPoolConfiguration(processingThreadPoolConfig);
    return context;
  }
}
