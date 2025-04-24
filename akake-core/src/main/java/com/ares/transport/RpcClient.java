package com.ares.transport;

import com.ares.common.ServiceMeta;
import com.ares.common.exception.RpcException;

public interface RpcClient {

  void start(String host, Integer port) throws InterruptedException;

  void stop();

  Object send(ServiceMeta serviceMeta) throws RpcException;
}
