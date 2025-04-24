package com.ares.transport.netty4.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ares.codec.protocol.Message;
import com.ares.codec.serialization.Serialization;
import com.ares.codec.serialization.SerializationFactory;
import com.ares.common.constant.Constants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class NettyDecoder extends ByteToMessageDecoder {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

    if (in.readableBytes() < Constants.HEADER_LEN) {
      return;
    }

    in.markReaderIndex();
    byte[] magic = new byte[2];
    in.readBytes(magic);
    if (magic[0] != Constants.MAGIC[0] || magic[1] != Constants.MAGIC[1]) {
      in.resetReaderIndex();
      return;
    }
    Message<Object> message = Message.<Object>builder()
        .magic(magic)
        .version(in.readByte())
        .codec(in.readByte())
        .compress(in.readByte())
        .msgType(in.readByte())
        .state(in.readByte())
        .oneWay(in.readByte())
        .reqId(in.readLong())
        .build();

    int bodyLen = in.readInt();
    message.setBodyLen(bodyLen);
    if (bodyLen == 0) {
      message.setBody(new byte[0]);
      return;
    }

    if (in.readableBytes() < bodyLen) {
      in.resetReaderIndex();
      return;
    }

    byte[] byteBody = new byte[bodyLen];
    in.readBytes(byteBody);
    Serialization codec = SerializationFactory.getSerialization(message.getCodec());
    Object body = codec.deserialize(byteBody, Object.class);
    message.setBody(body);
    out.add(message);
    logger.info("decode message,reqId:{}, msgType: {}", message.getReqId(), message.getMsgType());
  }
}
