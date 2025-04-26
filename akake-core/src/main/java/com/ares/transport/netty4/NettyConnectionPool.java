package com.ares.transport.netty4;

import com.ares.common.config.PoolConfigProperties;
import com.ares.common.exception.RpcException;
import com.ares.common.pool.ConnectionValidator;
import com.ares.common.pool.TransferQueueConnectionPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public class NettyConnectionPool {

  private final TransferQueueConnectionPool<Channel> pool;

  public NettyConnectionPool(ChannelFuture channelFuture, PoolConfigProperties properties)
      throws RpcException {
    ConnectionValidator<Channel> validator = new NettyConnectionValidator();
    Supplier<Channel> connFactory = channelFuture::channel;
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
