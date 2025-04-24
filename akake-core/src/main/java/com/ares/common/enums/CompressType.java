package com.ares.common.enums;

import lombok.Getter;

@Getter
public enum CompressType {
  NONE((byte) 0, "none"), LZ4((byte) 1, "lz4"), SNAPPY((byte) 2, "snappy");

  private final byte code;
  private final String name;


  CompressType(byte code, String name) {
    this.code = code;
    this.name = name;
  }
}
