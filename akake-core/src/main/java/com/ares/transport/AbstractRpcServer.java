package com.ares.transport;

import com.ares.common.ServiceMeta;
import com.ares.common.config.ServerConfigProperties;
import com.ares.common.exception.RpcException;
import com.ares.common.helper.ServiceHelper;
import com.ares.registry.Registry;
import com.ares.registry.RegistryFactory;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRpcServer extends AbstractRpcChannel implements RpcServer {

  private static final Logger logger = LoggerFactory.getLogger(AbstractRpcServer.class);

  protected final ServerConfigProperties properties;
  private final Registry registry;
  protected final ConcurrentHashMap<String, Class<?>> serviceCache;
  private ServiceMeta serviceMeta;

  public AbstractRpcServer(ServerConfigProperties properties) throws Exception {
    this.properties = Objects.requireNonNull(properties, "properties cannot be null");
    this.registry = new RegistryFactory(properties.getRegistryConfigProperties()).getRegister();
    this.serviceCache = new ConcurrentHashMap<>();
  }

  @Override
  public void init(String host, Integer port) throws RpcException {
    try {
      validateInitParameters(host, port);
      if (!initialized.compareAndSet(false, true)) {
        logger.warn("Server already initialized");
        return;
      }
      start(host, port);
      this.serviceMeta = buildServiceMeta(host, port);
      registerService(serviceMeta);
      cacheService(serviceMeta);
      logger.info("Server initialized successfully on {}:{}", host, port);
      active.set(true);
    } catch (Exception e) {
      initialized.set(false);
      active.set(false);
      logger.error("Failed to initialize server on {}:{}", host, port, e);
      throw new RpcException("Server initialization failed", e);
    }
  }

  private void validateInitParameters(String host, Integer port) {
    Objects.requireNonNull(host, "host cannot be null");
    Objects.requireNonNull(port, "port cannot be null");
    if (port <= 0 || port > 65535) {
      throw new IllegalArgumentException("Invalid port number: " + port);
    }
  }

  private void registerService(ServiceMeta serviceMeta) throws Exception {
    try {
      registry.register(serviceMeta);
      logger.info("Service registered successfully: {}", serviceMeta.getServiceName());
    } catch (Exception e) {
      logger.error("Failed to register service: {}", serviceMeta.getServiceName(), e);
      throw e;
    }
  }

  private ServiceMeta buildServiceMeta(String host, Integer port) {
    return ServiceMeta.builder()
        .serviceClass(properties.getTargetClass())
        .serviceName(properties.getInterfaceRef())
        .version(properties.getVersion())
        .group(properties.getGroup())
        .serviceHost(host)
        .servicePort(port)
        .build();
  }

  private void cacheService(ServiceMeta serviceMeta) {
    String serviceKey = ServiceHelper.buildServiceKey(
        properties.getInterfaceRef(),
        properties.getVersion(),
        properties.getGroup());
    serviceCache.putIfAbsent(serviceKey, serviceMeta.getServiceClass());
    logger.debug("Service cached: {}", serviceKey);
  }

  @Override
  public void close() throws RpcException {
    if (initialized.compareAndSet(true, false)) {
      try {
        if (serviceMeta != null) {
          registry.unregister(serviceMeta);
          logger.info("Service unregistered: {}", serviceMeta.getServiceName());
        }
        if (registry != null) {
          registry.close();
        }
        serviceCache.clear();
        stop();
        logger.info("Server closed successfully");
        active.set(false);
      } catch (Exception e) {
        logger.error("Error while closing server", e);
        throw new RpcException("Failed to close server", e);
      }
    }
  }

}
