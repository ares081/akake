package com.ares.transport.netty4.pool;

import io.netty.channel.Channel;

public interface ChannelPoolHandler {

  void channelCreated(Channel ch) throws Exception;

  void channelAcquired(Channel ch) throws Exception;

  void channelReleased(Channel ch) throws Exception;
}
