package com.ares.transport.netty4.pool;

import io.netty.channel.Channel;

public class PooledChannel {

  final Channel channel;
  final long lastUsed;

  public PooledChannel(Channel channel, long lastUsed) {
    this.channel = channel;
    this.lastUsed = lastUsed;
  }
}
