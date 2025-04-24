package com.ares.common.enums;

import lombok.Getter;

@Getter
public enum ChannelState {
  CONNECTED(1, "connected"), DISCONNECTED(2, "discnnected"), WORKING(3, "working");

  private final int code;
  private final String name;

  ChannelState(int code, String name) {
    this.code = code;
    this.name = name;
  }
}
