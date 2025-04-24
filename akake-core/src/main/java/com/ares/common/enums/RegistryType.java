package com.ares.common.enums;

import lombok.Getter;

@Getter
public enum RegistryType {
  /**
   * Nacos
   */
  NACOS(1, "nacos"),
  /**
   * Consul
   */
  CONSUL(3, "consul"),
  /**
   * Zookeeper
   */
  ZOOKEEPER(2, "zookeeper");

  private final int code;
  private final String name;

  RegistryType(int code, String name) {
    this.code = code;
    this.name = name;
  }

}