package com.ares.transport;

public interface RpcServer {

  void start(String host, Integer port) throws InterruptedException;

  void stop();
}
