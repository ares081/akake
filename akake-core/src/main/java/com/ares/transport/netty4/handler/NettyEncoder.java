package com.ares.transport.netty4.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ares.codec.protocol.Message;
import com.ares.codec.serialization.Serialization;
import com.ares.codec.serialization.SerializationFactory;
import com.ares.common.constant.Constants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder<T> extends MessageToByteEncoder<Message<T>> {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  protected void encode(ChannelHandlerContext ctx, Message<T> msg, ByteBuf out) throws Exception {
    logger.info("encoder receive msg, reqId: {}", msg.getReqId());
    // Write the message length
    out.writeBytes(Constants.MAGIC);
    out.writeByte(msg.getVersion());
    out.writeByte(msg.getCodec());
    out.writeByte(msg.getCompress());
    out.writeByte(msg.getMsgType());
    out.writeByte(msg.getState());
    out.writeByte(msg.getOneWay());
    out.writeLong(msg.getReqId());
    T body = msg.getBody();
    // Write the message body
    if (body != null) {
      Serialization codec = SerializationFactory.getSerialization(msg.getCodec());
      byte[] byteBody = codec.serialize(body);
      out.writeInt(byteBody.length);
      out.writeBytes(byteBody);
    } else {
      out.writeInt(0);
      out.writeBytes(new byte[0]);
    }
  }
}
