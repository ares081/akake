package com.ares.common.config;

import com.ares.common.enums.RegistryType;

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
public class RegistryConfigProperties {

  @Builder.Default
  private String path = "/akk_rpc";
  @Builder.Default
  private String registerType = RegistryType.ZOOKEEPER.getName();
  @Builder.Default
  private String registerAddress = "127.0.0.1";
  @Builder.Default
  private Integer registerPort = 2181;
}
