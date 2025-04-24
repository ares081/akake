package com.ares.common.enums;

import lombok.Getter;

@Getter
public enum RpcStatus {

  ok((byte) 1, "success"), failed((byte) 2, "failed");

  private final byte code;
  private final String name;

  RpcStatus(byte code, String name) {
    this.code = code;
    this.name = name;
  }

}