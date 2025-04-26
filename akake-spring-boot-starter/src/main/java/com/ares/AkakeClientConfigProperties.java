package com.ares;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.ares.common.constant.Constants;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "akake.client")
public class AkakeClientConfigProperties {
  private String group = Constants.DEFAULT_GROUP;
  private String application;
  private String serviceRef;
  private String serviceVersion = Constants.DEFAULT_SERVICE_VERSION;
  // 与连接相关的参数配置
  private Integer connectTimeout;
  private Integer readTimeout;
  private Integer maxConnections;
  // tcp参数
  public Boolean tcpNoDelay;
  public Boolean soKeepAlive;
  public Boolean soReuseAddr;
}
