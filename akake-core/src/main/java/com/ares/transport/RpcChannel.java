package com.ares.transport;

public interface RpcChannel {
  void close();

  boolean isActive();
}
