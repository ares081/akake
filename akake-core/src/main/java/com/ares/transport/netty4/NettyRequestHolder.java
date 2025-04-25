package com.ares.transport.netty4;

import com.ares.common.RpcResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyRequestHolder {

  public static final Map<Long, NettyFuture<RpcResponse>> REQUEST_MAP = new ConcurrentHashMap<>();
}
