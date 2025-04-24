package com.ares.common.config;

import com.ares.common.constant.Constants;
import com.ares.common.enums.CompressType;
import com.ares.common.enums.SerializationType;

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
public class ClientConfigProperties {

  @Builder.Default
  private String protocol = Constants.DEFAULT_PROTOCOL;

  @Builder.Default
  private String codec = SerializationType.HESSIAN.getName();

  @Builder.Default
  private String compress = CompressType.NONE.getName();

  @Builder.Default
  private String group = Constants.DEFAULT_GROUP;

  private String application;

  private String serviceName;

  @Builder.Default
  private String serviceVersion = Constants.DEFAULT_SERVICE_VERSION;

  private Class<?> targetClass;

  // 注册中心参数
  private RegistryConfigProperties registerProperties;

  // 与连接相关的参数配置
  private Integer connectTimeout;
  private Integer readTimeout;
  // tcp参数
  public Boolean tcpNoDelay;
  public Boolean soKeepAlive;
  public Boolean soReuseAddr;
}
