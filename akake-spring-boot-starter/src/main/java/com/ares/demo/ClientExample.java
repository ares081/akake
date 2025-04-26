package com.ares.demo;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.ares.transport.netty4.NettyInvocationHandler;

@Component
public class ClientExample {

  @Autowired
  private NettyInvocationHandler nettyInvocationHandler;

  @Bean
  public HelloService helloService() {
    HelloService helloService = (HelloService) Proxy.newProxyInstance(
        HelloService.class.getClassLoader(), new Class[] { HelloService.class },
        nettyInvocationHandler);
    String result = helloService.sayHello("server");
    System.out.println(result);
    return helloService;
  }
}
