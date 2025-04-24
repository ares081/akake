package com.ares.registry;

import com.ares.common.config.RegistryConfigProperties;
import com.ares.common.enums.RegistryType;

public class RegistryFactory {

  private final RegistryConfigProperties properties;
  private final Registry registry;

  public RegistryFactory(RegistryConfigProperties properties) throws Exception {
    this.properties = properties;
    this.registry = this.init(this.properties);
  }

  public Registry init(RegistryConfigProperties properties) throws Exception {
    if (properties.getRegisterType().equals(RegistryType.ZOOKEEPER.getName())) {
      return new ZKRegister(properties);
    }
    return new ZKRegister(properties);
  }

  public Registry getRegister() {
    return this.registry;
  }
}
