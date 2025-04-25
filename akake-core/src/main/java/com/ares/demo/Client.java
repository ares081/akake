package com.ares.demo;

import com.ares.common.config.ClientConfigProperties;
import com.ares.common.config.RegistryConfigProperties;
import com.ares.transport.netty4.NettyInvocationHandler;

public class Client {

  public static void main(String[] args) throws Exception {
    ClientConfigProperties properties = ClientConfigProperties.builder()
        .group("ares")
        .application("demo")
        .serviceRef(HelloService.class.getName())
        .serviceVersion("1.0.0")
        .connectTimeout(1000)
        .readTimeout(3000)
        .build();
    RegistryConfigProperties registerProperties = new RegistryConfigProperties();
    properties.setRegisterProperties(registerProperties);
    HelloService service = NettyInvocationHandler.newInstance(HelloService.class, properties);
    String result = service.sayHello("server");
    System.out.println(result);
  }
}
