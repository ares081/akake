package com.ares.transport.netty4;

import com.ares.codec.protocol.Message;
import com.ares.common.RpcResponse;
import com.ares.common.config.ClientConfigProperties;
import com.ares.common.exception.RpcException;
import com.ares.common.helper.NamedThreadFactory;
import com.ares.common.pool.TransferQueueConnectionPool;
import com.ares.transport.AbstractRpcClient;
import com.ares.transport.netty4.handler.NettyClientHandler;
import com.ares.transport.netty4.handler.NettyDecoder;
import com.ares.transport.netty4.handler.NettyEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClient extends AbstractRpcClient {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final Bootstrap bootstrap;
  private final NioEventLoopGroup group;
  private ChannelFuture channelFuture;
  private TransferQueueConnectionPool<Channel> channelPool;


  public NettyClient(ClientConfigProperties properties) throws Exception {
    super(properties);
    NamedThreadFactory groupFactory = new NamedThreadFactory("netty-client-pool");
    this.bootstrap = new Bootstrap();
    this.group = new NioEventLoopGroup(groupFactory);
    bootstrap.group(group)
        .channel(NioSocketChannel.class)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline()
                .addLast("decoder", new NettyDecoder())
                .addLast("encoder", new NettyEncoder<>())
                .addLast("clientHandler", new NettyClientHandler());
          }
        });
    if (properties.getSoKeepAlive() != null) {
      bootstrap.option(ChannelOption.SO_KEEPALIVE, properties.getSoKeepAlive());
    }
    if (properties.getTcpNoDelay() != null) {
      bootstrap.option(ChannelOption.TCP_NODELAY, properties.getTcpNoDelay());
    }
    if (properties.getSoReuseAddr() != null) {
      bootstrap.option(ChannelOption.SO_REUSEADDR, properties.getSoReuseAddr());
    }
  }

  @Override
  public void start(String host, Integer port) throws RpcException {
    bootstrap.remoteAddress(host, port);
    try {
      channelFuture = bootstrap.connect().sync();
    } catch (InterruptedException e) {
      throw new RpcException(e);
    }
    channelPool = new NettyConnectionPool(channelFuture, properties.getPoolProperties()).getPool();
  }

  @Override
  protected Object doSend(Message<byte[]> message) throws RpcException {
    checkActive();
    Channel channel = null;
    final NettyFuture<RpcResponse> future = new NettyFuture<>(
        new DefaultPromise<>(new DefaultEventLoop()), properties.getReadTimeout());
    try {
      NettyRequestHolder.REQUEST_MAP.put(message.getReqId(), future);
      channel = acquireChannel();
      channel.writeAndFlush(message).addListener(f -> {
        if (!f.isSuccess()) {
          future.getPromise().setFailure(f.cause());
          NettyRequestHolder.REQUEST_MAP.remove(message.getReqId());
        }
      });
      RpcResponse response = future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS);
      if (response == null) {
        throw new RpcException("Received null response");
      }
      return response.getData();
    } catch (TimeoutException e) {
      throw new RpcException("Request timeout after " + properties.getReadTimeout() + "ms", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RpcException("Request interrupted", e);
    } catch (Exception e) {
      throw new RpcException("Failed to send request", e);
    } finally {
      if (channel != null) {
        releaseChannel(channel);
      }
      NettyRequestHolder.REQUEST_MAP.remove(message.getReqId());
    }
  }

  private Channel acquireChannel() throws Exception {
    Channel channel = channelPool.acquire(1000, TimeUnit.MILLISECONDS);
    if (channel == null || !channel.isActive()) {
      throw new RpcException("Failed to acquire active channel");
    }
    return channel;
  }

  private void releaseChannel(Channel channel) {
    try {
      channelPool.release(channel);
    } catch (Exception e) {
      logger.warn("Failed to release channel", e);
    }
  }

  private void checkActive() throws RpcException {
    if (!isActive()) {
      throw new RpcException("NettyClient is not active, please check if it is properly started");
    }
  }

  @Override
  public void stop() {
    if (!initialized.get()) {
      return;
    }
    try {
      if (channelPool != null) {
        channelPool.close();
      }
      if (channelFuture != null) {
        ChannelFuture closeFuture = channelFuture.channel().closeFuture().sync();
        closeFuture.addListener(future -> {
          if (future.isSuccess()) {
            closeFuture.channel().close();
          }
        });
      }
      if (group != null) {
        group.shutdownGracefully(100, 1000, TimeUnit.MILLISECONDS).sync();
      }
      logger.info("NettyClient stopped successfully");
    } catch (Exception e) {
      logger.error("Error while stopping NettyClient", e);
    }
  }
}
