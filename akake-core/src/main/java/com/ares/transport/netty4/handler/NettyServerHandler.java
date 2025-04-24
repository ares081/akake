package com.ares.transport.netty4.handler;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ares.codec.protocol.Message;
import com.ares.common.RpcRequest;
import com.ares.common.RpcResponse;
import com.ares.common.enums.MsgType;
import com.ares.common.enums.RpcStatus;
import com.ares.common.exception.RpcException;
import com.ares.common.helper.ServiceHelper;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyServerHandler extends SimpleChannelInboundHandler<Message<RpcRequest>> {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  protected final ConcurrentHashMap<String, Class<?>> serviceCache;

  public NettyServerHandler(ConcurrentHashMap<String, Class<?>> serviceCache) {
    this.serviceCache = serviceCache;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message<RpcRequest> msg) throws Exception {
    MsgType msgType = MsgType.getMsgType(msg.getMsgType());
    if (msgType == MsgType.REQUEST) {
      Object object = requestInvoke(msg.getBody());
      RpcResponse response = new RpcResponse(msg.getReqId(), (int) RpcStatus.ok.getCode(), object, null);
      Message<RpcResponse> rpcMessage = Message.<RpcResponse>builder()
          .magic(msg.getMagic())
          .version(msg.getVersion())
          .codec(msg.getCodec())
          .compress(msg.getCompress())
          .msgType(MsgType.RESPONSE.getCode())
          .state(RpcStatus.ok.getCode())
          .oneWay(msg.getOneWay())
          .reqId(msg.getReqId())
          .build();
      rpcMessage.setBodyLen(0);
      rpcMessage.setBody(response);
      ctx.writeAndFlush(rpcMessage);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    super.exceptionCaught(ctx, cause);
  }

  private Object requestInvoke(RpcRequest request) throws Exception {
    String serviceKey = ServiceHelper.buildServiceKey(request.getServiceName(),
        request.getServiceVersion(), request.getGroup());
    Class<?> service = serviceCache.get(serviceKey);
    if (service == null) {
      throw new RpcException("service not found" + serviceKey);
    }
    Method method = service.getMethod(request.getMethodName(), request.getParamTypes());
    return method.invoke(service.getDeclaredConstructor().newInstance(), request.getParams());
  }
}
