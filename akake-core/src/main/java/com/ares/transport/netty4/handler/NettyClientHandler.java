package com.ares.transport.netty4.handler;

import com.ares.codec.protocol.Message;
import com.ares.common.RpcResponse;
import com.ares.transport.netty4.NettyFuture;
import com.ares.transport.netty4.NettyRequestHolder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientHandler extends SimpleChannelInboundHandler<Message<RpcResponse>> {
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message<RpcResponse> msg) throws Exception {
    long requestId = msg.getReqId();
    NettyFuture<RpcResponse> future = NettyRequestHolder.REQUEST_MAP.get(requestId);
    future.getPromise().setSuccess(msg.getBody());
  }
}
