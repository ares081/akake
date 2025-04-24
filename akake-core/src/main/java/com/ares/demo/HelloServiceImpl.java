package com.ares.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloServiceImpl implements HelloService {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public HelloServiceImpl() {
  }

  @Override
  public String sayHello(String name) {
    logger.info("hello:{} ", name);
    return "hello client";
  }
}
