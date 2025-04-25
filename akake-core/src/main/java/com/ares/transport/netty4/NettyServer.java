package com.ares.transport.netty4;

import com.ares.common.config.ServerConfigProperties;
import com.ares.common.helper.NamedThreadFactory;
import com.ares.transport.AbstractRpcServer;
import com.ares.transport.netty4.handler.NettyDecoder;
import com.ares.transport.netty4.handler.NettyEncoder;
import com.ares.transport.netty4.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer extends AbstractRpcServer {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final ServerBootstrap bootstrap;
  private final NioEventLoopGroup boss;
  private final NioEventLoopGroup worker;
  private ChannelFuture channelFuture;

  public NettyServer(ServerConfigProperties properties) throws Exception {
    super(properties);
    this.bootstrap = new ServerBootstrap();
    NamedThreadFactory bossFactory = new NamedThreadFactory("netty-server-worker-pool");
    NamedThreadFactory workFactory = new NamedThreadFactory("netty-server-worker-pool");
    this.boss = new NioEventLoopGroup(1, bossFactory);
    this.worker = new NioEventLoopGroup(workFactory);
  }

  @Override
  public void start(String host, Integer port) throws InterruptedException {
    bootstrap.group(boss, worker)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline()
                .addLast("decoder", new NettyDecoder())
                .addLast("encoder", new NettyEncoder<>())
                .addLast("requestHandler", new NettyServerHandler(serviceCache));
          }
        });
    if (properties.getSoBacklog() > 0) {
      bootstrap.option(ChannelOption.SO_BACKLOG, properties.getSoBacklog());
    }
    if (properties.getTcpNoDelay() != null) {
      bootstrap.option(ChannelOption.TCP_NODELAY, true);
    }
    this.channelFuture = bootstrap.bind(host, port).sync();
  }

  @Override
  public void stop() {
    try {
      if (channelFuture != null) {
        // Close the server channel
        channelFuture.channel().close().sync();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.warn("Interrupted while closing server channel", e);
    } finally {
      // Gracefully shutdown the event loop groups
      if (boss != null) {
        boss.shutdownGracefully(0, 1000, TimeUnit.MILLISECONDS);
      }
      if (worker != null) {
        worker.shutdownGracefully(0, 1000, TimeUnit.MILLISECONDS);
      }
      logger.info("Netty server resources released");
    }
  }

}
