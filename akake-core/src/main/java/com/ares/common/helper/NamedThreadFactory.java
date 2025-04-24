package com.ares.common.helper;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

  static final AtomicInteger poolNo = new AtomicInteger(1);
  private final AtomicInteger threadNo = new AtomicInteger(1);
  final ThreadGroup group;
  final String namePrefix;
  final boolean isDaemon;

  public NamedThreadFactory() {
    this("pool");
  }

  public NamedThreadFactory(String name) {
    this(name, false);
  }

  public NamedThreadFactory(String preffix, boolean daemon) {
    group = Thread.currentThread().getThreadGroup();
    namePrefix = preffix + "-" + poolNo.getAndIncrement() + "-thread-";
    isDaemon = daemon;
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread thread = new Thread(group, r, namePrefix + threadNo.getAndIncrement(), 0);
    thread.setDaemon(isDaemon);
    if (thread.getPriority() != Thread.NORM_PRIORITY) {
      thread.setPriority(Thread.NORM_PRIORITY);
    }
    return thread;
  }
}
