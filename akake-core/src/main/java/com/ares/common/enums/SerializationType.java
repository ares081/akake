package com.ares.common.enums;

import lombok.Getter;

@Getter
public enum SerializationType {
  HESSIAN((byte) 1, "hessian"), PROTOBUF((byte) 2, "protobuf");

  private final byte code;
  private final String name;

  SerializationType(byte code, String name) {
    this.code = code;
    this.name = name;
  }
}
