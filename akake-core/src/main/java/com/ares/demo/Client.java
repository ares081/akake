package com.ares.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ares.common.config.ClientConfigProperties;
import com.ares.common.config.PoolConfigProperties;
import com.ares.common.config.RegistryConfigProperties;
import com.ares.transport.netty4.NettyInvocationHandler;

public class Client {
  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  public static void main(String[] args) throws Exception {
    ClientConfigProperties properties = ClientConfigProperties.builder()
        .group("ares")
        .application("demo")
        .serviceRef(HelloService.class.getName())
        .serviceVersion("1.0.0")
        .connectTimeout(1000)
        .readTimeout(3000)
        .build();
    PoolConfigProperties poolProperties = new PoolConfigProperties(20, 200, 60000L, 5000L, 60000L);
    RegistryConfigProperties registerProperties = new RegistryConfigProperties();
    properties.setRegisterProperties(registerProperties);
    properties.setPoolProperties(poolProperties);
    HelloService service = NettyInvocationHandler.newInstance(HelloService.class, properties);

    for (int i = 0; i < 5; i++) {
      String result = service.sayHello("server");
      logger.info("get msg from server: {}", result);
    }
  }
}
