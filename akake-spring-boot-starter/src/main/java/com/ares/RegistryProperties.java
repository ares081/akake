package com.ares;

import com.ares.common.enums.RegistryType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "akake.registry")
public class RegistryProperties {

  private String path = "/akk_rpc";
  private String registerType = RegistryType.ZOOKEEPER.getName();
  private String registerAddress = "127.0.0.1";
  private Integer registerPort = 2181;
}
