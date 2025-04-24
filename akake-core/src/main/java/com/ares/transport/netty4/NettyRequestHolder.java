package com.ares.transport.netty4;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ares.common.RpcResponse;

public class NettyRequestHolder {
  public static final Map<Long, NettyFuture<RpcResponse>> REQUEST_MAP = new ConcurrentHashMap<>();
}
