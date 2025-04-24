package com.ares.transport;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ares.common.ServiceMeta;
import com.ares.common.config.ServerConfigProperties;
import com.ares.common.helper.ServiceHelper;
import com.ares.registry.Registry;
import com.ares.registry.RegistryFactory;

public abstract class AbstractRpcServer extends AbstractRpcChannel implements RpcServer {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  protected final ServerConfigProperties properties;
  private final Registry registry;
  protected final ConcurrentHashMap<String, Class<?>> serviceCache = new ConcurrentHashMap<>();

  public AbstractRpcServer(ServerConfigProperties properties) throws Exception {
    this.properties = properties;
    this.registry = new RegistryFactory(properties.getRegistryConfigProperties()).getRegister();
  }

  @Override
  public void init(String host, Integer port) {
    try {
      start(host, port);
    } catch (InterruptedException e) {
      logger.error("server start failed, host: {}, port: {}", host, port);
    }
    ServiceMeta serviceMeta = ServiceMeta.builder()
        .serviceClass(properties.getServiceClass())
        .serviceName(properties.getServiceName())
        .version(properties.getVersion())
        .group(properties.getGroup())
        .serviceHost(host)
        .servicePort(port)
        .build();
    try {
      registerService(serviceMeta);
    } catch (Exception e) {
      logger.error("service register faild, service: {}", serviceMeta.getServiceName());
    }
    String serviceKey = ServiceHelper.buildServiceKey(properties.getServiceName(), properties.getVersion(),
        properties.getGroup());
    serviceCache.putIfAbsent(serviceKey, serviceMeta.getServiceClass());
  }

  private void registerService(ServiceMeta serviceMeta) throws Exception {
    registry.register(serviceMeta);
  }

  /**
   * close channel
   */
  @Override
  public void close() {

  }
}
