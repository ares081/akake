package com.ares.common.enums;

import lombok.Getter;

@Getter
public enum MsgType {
  HEART_BEAT((byte) 0, "heart_beat"), REQUEST((byte) 1, "request"), RESPONSE((byte) 2, "response"),
  EXCEPTION((byte) 3, "exception");

  private final byte code;
  private final String name;

  MsgType(byte code, String name) {
    this.code = code;
    this.name = name;
  }

  public static MsgType getMsgType(int code) {
    for (MsgType msgType : MsgType.values()) {
      if (msgType.getCode() == code) {
        return msgType;
      }
    }
    return null;
  }
}
