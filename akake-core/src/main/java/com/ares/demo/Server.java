package com.ares.demo;

import com.ares.common.config.RegistryConfigProperties;
import com.ares.common.config.ServerConfigProperties;
import com.ares.transport.AbstractRpcServer;
import com.ares.transport.netty4.NettyServer;
import java.net.InetAddress;

public class Server {

  public static void main(String[] args) throws Exception {
    ServerConfigProperties properties = ServerConfigProperties.builder()
        .group("ares")
        .application("rpc")
        .interfaceRef(HelloService.class.getName())
        .targetClass(HelloServiceImpl.class)
        .version("1.0.0")
        .build();
    RegistryConfigProperties registerProperties = new RegistryConfigProperties();
    properties.setRegistryConfigProperties(registerProperties);
    AbstractRpcServer server = new NettyServer(properties);
    String host = InetAddress.getLocalHost().getHostAddress();
    server.init(host, properties.getPort());
  }
}
