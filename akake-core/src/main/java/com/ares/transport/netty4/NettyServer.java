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

public class NettyServer extends AbstractRpcServer {

  private final ServerBootstrap bootstrap;
  private final NioEventLoopGroup boss;
  private final NioEventLoopGroup worker;

  public NettyServer(ServerConfigProperties properties) throws Exception {
    super(properties);
    this.bootstrap = new ServerBootstrap();
    NamedThreadFactory bossFactory = new NamedThreadFactory("netty-server-boss-pool");
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
    if (properties.getTcpNoDelay() !=null) {
      bootstrap.option(ChannelOption.TCP_NODELAY, true);
    }
    ChannelFuture channelFuture = bootstrap.bind(host, port).sync();
    channelFuture.channel().closeFuture();
  }

  @Override
  public void stop() {

  }
}
