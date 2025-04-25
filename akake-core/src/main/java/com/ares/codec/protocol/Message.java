package com.ares.codec.protocol;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message<T> implements Serializable {

  @Serial
  private static final long serialVersionUID = 6669066249377691018L;
  // 2+1+1+1+1+1+1+8+4 =20
  // 魔数，2字节
  private byte[] magic;
  // 版本，1字节
  private byte version;
  // 序列化方式，1字节
  private byte codec;
  // 压缩方式，1字节
  private byte compress;
  // 消息类型，1字节
  private byte msgType;
  // 状态，1字节
  private byte state;
  // 单工，1字节
  private byte oneWay;
  // 消息ID，8字节
  private long reqId;
  // 消息体长度，4字节
  private int bodyLen;

  private T body;
}
