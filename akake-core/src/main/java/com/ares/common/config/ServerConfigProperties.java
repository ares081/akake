package com.ares.common.config;

import com.ares.common.constant.Constants;
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
public class ServerConfigProperties {

  @Builder.Default
  private int port = Constants.DEFAULT_PORT;
  private String application;
  private String interfaceRef;
  private Class<?> targetClass;
  @Builder.Default
  private String group = Constants.DEFAULT_GROUP;
  @Builder.Default
  private String version = Constants.DEFAULT_SERVICE_VERSION;
  @Builder.Default
  public Integer coreThread = Runtime.getRuntime().availableProcessors() * 2;
  @Builder.Default
  public Integer maxThreads = Constants.DEFAULT_MAX_WORKER_THREAD;

  public int soBacklog;
  public Boolean tcpNoDelay;
  private RegistryConfigProperties registryConfigProperties;
}
