package com.ares.transport.netty4;

import java.util.function.Supplier;

import com.ares.common.config.PoolConfigProperties;
import com.ares.common.exception.RpcException;
import com.ares.common.pool.ConnectionValidator;
import com.ares.common.pool.TransferQueueConnectionPool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Getter;

@Getter
public class NettyConnectionPool {

  private final TransferQueueConnectionPool<Channel> pool;
  private final ChannelFuture channelFuture;

  public NettyConnectionPool(Bootstrap bootstrap, PoolConfigProperties properties) throws RpcException {
    ConnectionValidator<Channel> validator = new NettyConnectionValidator();
    try {
      this.channelFuture = bootstrap.connect().sync();
    } catch (InterruptedException e) {
      throw new RpcException(e);
    }
    // Supplier<Channel> connFactory = () -> bootstrap.clone().connect().channel();
    Supplier<Channel> connFactory = () -> channelFuture.channel();
    this.pool = new TransferQueueConnectionPool<>(connFactory, validator, properties);
  }

  private static class NettyConnectionValidator implements ConnectionValidator<Channel> {
    @Override
    public boolean validate(Channel channel) {
      return channel != null && channel.isActive();
    }

    @Override
    public void closeConnection(Channel channel) {
      channel.close();
    }
  }
}
