package com.ares.transport.netty4.handler;

import com.ares.codec.protocol.Message;
import com.ares.common.RpcRequest;
import com.ares.common.RpcResponse;
import com.ares.common.enums.MsgType;
import com.ares.common.enums.RpcStatus;
import com.ares.common.exception.RpcException;
import com.ares.common.helper.ServiceHelper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServerHandler extends SimpleChannelInboundHandler<Message<RpcRequest>> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  protected final ConcurrentHashMap<String, Class<?>> serviceCache;

  public NettyServerHandler(ConcurrentHashMap<String, Class<?>> serviceCache) {
    this.serviceCache = serviceCache;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message<RpcRequest> msg) {
    try {
      MsgType msgType = MsgType.getMsgType(msg.getMsgType());
      switch (Objects.requireNonNull(msgType)) {
        case REQUEST:
          handleRequest(ctx, msg);
          break;
        case HEART_BEAT:
          handleHeartbeat(ctx, msg);
          break;
        default:
          logger.warn("Unsupported message type: {}", msgType);
      }
    } catch (Exception e) {
      handleException(ctx, msg, e);
    }
  }

  private void handleRequest(ChannelHandlerContext ctx, Message<RpcRequest> msg) {
    try {
      Object result = requestInvoke(msg.getBody());
      sendResponse(ctx, msg, result, null);
    } catch (Exception e) {
      handleException(ctx, msg, e);
    }
  }

  private void handleHeartbeat(ChannelHandlerContext ctx, Message<RpcRequest> msg) {
    Message<RpcResponse> heartbeatResponse = createResponseMessage(msg, null);
    heartbeatResponse.setMsgType(MsgType.HEART_BEAT.getCode());
    ctx.writeAndFlush(heartbeatResponse)
        .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    logger.debug("Sent heartbeat response to client: {}", ctx.channel().remoteAddress());
  }

  private void handleException(ChannelHandlerContext ctx, Message<RpcRequest> msg, Exception e) {
    logger.error("Error processing request", e);
    String errorMessage = e.getMessage();
    if (e instanceof RpcException) {
      sendResponse(ctx, msg, null, errorMessage);
    } else {
      sendResponse(ctx, msg, null, "Internal server error: " + errorMessage);
    }
  }

  private void sendResponse(ChannelHandlerContext ctx, Message<RpcRequest> request,
      Object result, String errorMsg) {
    RpcResponse response = new RpcResponse(
        request.getReqId(),
        errorMsg == null ? (int) RpcStatus.ok.getCode() : (int) RpcStatus.failed.getCode(),
        result, errorMsg == null ? null : new RpcException(errorMsg));

    Message<RpcResponse> rpcMessage = createResponseMessage(request, response);
    ctx.writeAndFlush(rpcMessage)
        .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
  }

  private Message<RpcResponse> createResponseMessage(Message<RpcRequest> request,
      RpcResponse response) {
    return Message.<RpcResponse>builder()
        .magic(request.getMagic())
        .version(request.getVersion())
        .codec(request.getCodec())
        .compress(request.getCompress())
        .msgType(MsgType.RESPONSE.getCode())
        .state(response == null ? RpcStatus.ok.getCode() : RpcStatus.failed.getCode())
        .oneWay(request.getOneWay())
        .reqId(request.getReqId())
        .body(response)
        .build();
  }

  private Object requestInvoke(RpcRequest request) throws Exception {
    String serviceKey = ServiceHelper.buildServiceKey(
        request.getServiceName(),
        request.getServiceVersion(),
        request.getGroup());

    Class<?> serviceClass = serviceCache.get(serviceKey);
    if (serviceClass == null) {
      throw new RpcException("Service not found: " + serviceKey);
    }

    try {
      Method method = serviceClass.getMethod(request.getMethodName(), request.getParamTypes());
      Object serviceInstance = serviceClass.getDeclaredConstructor().newInstance();
      return method.invoke(serviceInstance, request.getParams());
    } catch (NoSuchMethodException e) {
      throw new RpcException("Method not found: " + request.getMethodName());
    } catch (Exception e) {
      throw new RpcException("Failed to invoke service method: " + e.getMessage());
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    logger.error("Channel exception caught", cause);
    ctx.close();
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    logger.info("Client connected: {}", ctx.channel().remoteAddress());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    logger.info("Client disconnected: {}", ctx.channel().remoteAddress());
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent event = (IdleStateEvent) evt;
      if (event.state() == IdleState.READER_IDLE) {
        logger.warn("Reader idle timeout, closing channel: {}", ctx.channel().remoteAddress());
        ctx.close();
      }
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }
}
