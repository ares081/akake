package com.ares;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.ares.common.constant.Constants;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "akake.server")
public class AkakeServerConfigProperties {

  private int port = Constants.DEFAULT_PORT;
  private String application;
  private String interfaceName;
  private String targetClassRef;
  private Class<?> targetClass;
  private String group = Constants.DEFAULT_GROUP;
  private String version = Constants.DEFAULT_SERVICE_VERSION;
  public int coreThread = Runtime.getRuntime().availableProcessors() * 2;
  public int maxThreads = Constants.DEFAULT_MAX_WORKER_THREAD;
  public int soBacklog;
  public Boolean tcpNoDelay;
}
