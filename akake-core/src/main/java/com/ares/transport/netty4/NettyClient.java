package com.ares.transport.netty4;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ares.codec.protocol.Message;
import com.ares.common.RpcResponse;
import com.ares.common.config.ClientConfigProperties;
import com.ares.common.exception.RpcException;
import com.ares.common.helper.NamedThreadFactory;
import com.ares.transport.AbstractRpcClient;
import com.ares.transport.netty4.handler.NettyClientHandler;
import com.ares.transport.netty4.handler.NettyDecoder;
import com.ares.transport.netty4.handler.NettyEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;

public class NettyClient extends AbstractRpcClient {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final Bootstrap bootstrap;
  private final NioEventLoopGroup group;
  // todo Channel 池化
  private ChannelFuture channelFuture;

  public NettyClient(ClientConfigProperties properties) throws Exception {
    super(properties);
    NamedThreadFactory groupFactory = new NamedThreadFactory("client-group");
    this.bootstrap = new Bootstrap();
    this.group = new NioEventLoopGroup(groupFactory);
  }

  @Override
  public void start(String host, Integer port) throws InterruptedException {
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
      this.bootstrap.option(ChannelOption.TCP_NODELAY, properties.getTcpNoDelay());
    }
    if (properties.getSoReuseAddr() != null) {
      this.bootstrap.option(ChannelOption.SO_REUSEADDR, properties.getSoReuseAddr());
    }
    // todo 这里需要放到 channelpool 中
    ChannelFuture future = bootstrap.connect(host, port).sync();
    this.channelFuture = future;
    setActive(true);
    // channelFuture.channel().closeFuture();
  }

  @Override
  protected Object doSend(Message<byte[]> message) throws RpcException {
    NettyFuture<RpcResponse> future = new NettyFuture<>(new DefaultPromise<>(new DefaultEventLoop()),
        properties.getReadTimeout());
    NettyRequestHolder.REQUEST_MAP.put(message.getReqId(), future);
    try {
      channelFuture.channel().writeAndFlush(message);
      return future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS).getData();
    } catch (Exception e) {
      throw new RpcException("Failed to get response", e);
    }
  }

  @Override
  public void stop() {

  }

}
