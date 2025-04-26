package com.ares.transport;

import com.ares.common.pool.ConnectionValidator;
import com.ares.common.pool.PoolConfigProperties;
import com.ares.common.pool.TransferQueueConnectionPool;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import java.util.function.Supplier;
import lombok.Getter;

public class NettyConnectionPool {

  @Getter
  private final TransferQueueConnectionPool<Channel> pool;

  public NettyConnectionPool(Bootstrap bootstrap, PoolConfigProperties properties) {
    ConnectionValidator<Channel> validator = new NettyConnectionValidator();
    Supplier<Channel> connFactory = () -> bootstrap.clone().connect().channel();
    this.pool = new TransferQueueConnectionPool<>(connFactory, validator, properties);
  }

  static class NettyConnectionValidator implements ConnectionValidator<Channel> {

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
