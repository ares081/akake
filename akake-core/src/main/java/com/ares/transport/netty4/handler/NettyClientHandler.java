package com.ares.transport.netty4.handler;

import com.ares.codec.protocol.Message;
import com.ares.common.RpcResponse;
import com.ares.transport.netty4.NettyFuture;
import com.ares.transport.netty4.NettyRequestHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClientHandler extends SimpleChannelInboundHandler<Message<RpcResponse>> {

  private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message<RpcResponse> msg)
      throws Exception {
    try {
      long requestId = msg.getReqId();
      NettyFuture<RpcResponse> future = NettyRequestHolder.REQUEST_MAP.remove(requestId);

      if (future == null) {
        logger.warn("Response received but request future not found for id: {}", requestId);
        return;
      }

      RpcResponse response = msg.getBody();
      if (response == null) {
        future.getPromise().setFailure(new IllegalStateException("Received null response"));
        return;
      }

      future.getPromise().setSuccess(response);

    } catch (Exception e) {
      logger.error("Error processing response message", e);
      throw e;
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    logger.error("Channel exception caught", cause);
    // Find all pending requests for this channel and fail them
    NettyRequestHolder.REQUEST_MAP.forEach((requestId, future) -> {
      future.getPromise().setFailure(cause);
    });
    ctx.close();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    logger.warn("Channel inactive: {}", ctx.channel());
    // Handle pending requests when channel becomes inactive
    NettyRequestHolder.REQUEST_MAP.forEach((requestId, future) -> {
      future.getPromise().setFailure(
          new IllegalStateException("Channel closed unexpectedly"));
    });
    ctx.fireChannelInactive();
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent event = (IdleStateEvent) evt;
      if (event.state() == IdleState.READER_IDLE) {
        logger.warn("Reader idle timeout for channel: {}", ctx.channel());
        ctx.close();
      } else if (event.state() == IdleState.WRITER_IDLE) {
        logger.warn("Writer idle timeout for channel: {}", ctx.channel());
        ctx.close();
      }
    }
    super.userEventTriggered(ctx, evt);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    logger.debug("Channel active: {}", ctx.channel());
    ctx.fireChannelActive();
  }
}
