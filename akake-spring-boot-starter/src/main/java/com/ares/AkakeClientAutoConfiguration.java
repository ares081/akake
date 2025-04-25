package com.ares;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.ares.annotaion.EnableClient;
import com.ares.common.config.ClientConfigProperties;
import com.ares.common.config.RegistryConfigProperties;
import com.ares.common.exception.RpcException;
import com.ares.transport.netty4.NettyInvocationHandler;

@AutoConfiguration
@EnableConfigurationProperties({ AkakeClientConfigProperties.class, RegistryProperties.class })
@ConditionalOnProperty(prefix = "akake", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(annotation = EnableClient.class)
public class AkakeClientAutoConfiguration {

  @Bean
  public NettyInvocationHandler nettyInvocationHandler(
      AkakeClientConfigProperties akakeClientConfigProperties,
      @Autowired @Qualifier("registryProperties") RegistryProperties registryProperties)
      throws Exception {
    try {
      RegistryConfigProperties registryConfigProperties = RegistryConfigProperties.builder()
          .path(registryProperties.getPath())
          .registerAddress(registryProperties.getRegisterAddress())
          .registerPort(registryProperties.getRegisterPort())
          .registerType(registryProperties.getRegisterType())
          .build();
      ClientConfigProperties properties = ClientConfigProperties.builder()
          .group(akakeClientConfigProperties.getGroup())
          .application(akakeClientConfigProperties.getApplication())
          .serviceRef(akakeClientConfigProperties.getServiceRef())
          .serviceVersion(akakeClientConfigProperties.getServiceVersion())
          .registerProperties(registryConfigProperties)
          .connectTimeout(akakeClientConfigProperties.getConnectTimeout())
          .readTimeout(akakeClientConfigProperties.getReadTimeout())
          .maxConnections(akakeClientConfigProperties.getMaxConnections())
          .tcpNoDelay(akakeClientConfigProperties.getTcpNoDelay())
          .soKeepAlive(akakeClientConfigProperties.getSoKeepAlive())
          .soReuseAddr(akakeClientConfigProperties.getSoReuseAddr())
          .build();
      return new NettyInvocationHandler(properties);
    } catch (Exception e) {
      throw new RpcException("netty client start failed", e);
    }
  }
}
