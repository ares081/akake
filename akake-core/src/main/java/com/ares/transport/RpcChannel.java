package com.ares.transport;

import com.ares.common.exception.RpcException;

public interface RpcChannel {

  void close() throws RpcException;

  boolean isActive();
}
