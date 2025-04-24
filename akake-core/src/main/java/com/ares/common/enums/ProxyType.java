package com.ares.common.enums;

import lombok.Getter;

@Getter
public enum ProxyType {
  JDK(1, "jdk"),
  CGLIB(2, "cglib"),
  BYTEBUDDY(5, "bytebuddy");

  private final int code;
  private final String name;

  ProxyType(int code, String name) {
    this.code = code;
    this.name = name;
  }

  public int getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public static ProxyType fromCode(int code) {
    for (ProxyType type : values()) {
      if (type.code == code) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown proxy type code: " + code);
  }
}
