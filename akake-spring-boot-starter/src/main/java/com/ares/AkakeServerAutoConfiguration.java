package com.ares;

import java.net.InetAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.ares.annotaion.EnableServer;
import com.ares.common.config.RegistryConfigProperties;
import com.ares.common.config.ServerConfigProperties;
import com.ares.common.exception.RpcException;
import com.ares.transport.netty4.NettyServer;

@AutoConfiguration
@EnableConfigurationProperties({ AkakeServerConfigProperties.class, RegistryProperties.class })
@ConditionalOnProperty(prefix = "akake", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(annotation = EnableServer.class)
public class AkakeServerAutoConfiguration {

  @Bean
  public NettyServer nettyServer(AkakeServerConfigProperties akakeServerConfigProperties,
      @Autowired @Qualifier("registryProperties") RegistryProperties registryProperties)
      throws Exception {
    try {
      Class<?> targetClass = Class.forName(akakeServerConfigProperties.getTargetClassRef());
      String interfaceRef = "";
      for (Class<?> interRef : targetClass.getInterfaces()) {
        if (akakeServerConfigProperties.getInterfaceName().equals(interRef.getSimpleName())) {
          interfaceRef = interRef.getName();
          break;
        } else {
          throw new RpcException("interface not found");
        }
      }
      RegistryConfigProperties registryConfigProperties = RegistryConfigProperties.builder()
          .path(registryProperties.getPath())
          .registerAddress(registryProperties.getRegisterAddress())
          .registerPort(registryProperties.getRegisterPort())
          .registerType(registryProperties.getRegisterType())
          .build();
      ServerConfigProperties properties = ServerConfigProperties.builder()
          .application(akakeServerConfigProperties.getApplication())
          .interfaceRef(interfaceRef)
          .targetClass(targetClass)
          .group(akakeServerConfigProperties.getGroup())
          .port(akakeServerConfigProperties.getPort())
          .version(akakeServerConfigProperties.getVersion())
          .coreThread(akakeServerConfigProperties.getCoreThread())
          .maxThreads(akakeServerConfigProperties.getMaxThreads())
          .registryConfigProperties(registryConfigProperties)
          .build();
      NettyServer nettyServer = new NettyServer(properties);
      String address = InetAddress.getLocalHost().getHostAddress();
      nettyServer.init(address, properties.getPort());
      return nettyServer;
    } catch (Exception e) {
      throw new RpcException(e);
    }

  }
}
