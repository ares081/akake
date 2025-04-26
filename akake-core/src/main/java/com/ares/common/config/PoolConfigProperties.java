package com.ares.common.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class PoolConfigProperties {
  @Builder.Default
  private Integer minSize = 20;
  @Builder.Default
  private Integer maxSize = 200;
  @Builder.Default
  private Long maxIdleTime = 60000L;
  @Builder.Default
  private Long validationInterval = 60000L;
  @Builder.Default
  private Long maxLifetime = 10000L;
}
