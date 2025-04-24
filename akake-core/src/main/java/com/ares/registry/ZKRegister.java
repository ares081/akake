package com.ares.registry;

import java.util.Collection;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import com.ares.common.ServiceMeta;
import com.ares.common.config.RegistryConfigProperties;
import com.ares.common.helper.ServiceHelper;

public class ZKRegister implements Registry {

  private static final int DEFAULT_RETRY_TIMES = 3;
  private static final int DEFAULT_SLEEP_TIME_MILLS = 1000;
  private final ServiceDiscovery<ServiceMeta> serviceDiscovery;

  public ZKRegister(RegistryConfigProperties properties) throws Exception {
    CuratorFramework client = CuratorFrameworkFactory
        .newClient(properties.getRegisterAddress() + ":" + properties.getRegisterPort(),
            new ExponentialBackoffRetry(DEFAULT_SLEEP_TIME_MILLS, DEFAULT_RETRY_TIMES));
    client.start();
    JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(
        ServiceMeta.class);
    ServiceDiscoveryBuilder<ServiceMeta> builder = ServiceDiscoveryBuilder
        .builder(ServiceMeta.class)
        .client(client)
        .serializer(serializer)
        .basePath(properties.getPath());
    this.serviceDiscovery = builder.build();
    this.serviceDiscovery.start();
  }

  @Override
  public void register(ServiceMeta serviceMeta) throws Exception {
    ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
        .<ServiceMeta>builder()
        .name(ServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getVersion(),
            serviceMeta.getGroup()))
        .address(serviceMeta.getServiceHost())
        .port(serviceMeta.getServicePort())
        .payload(serviceMeta)
        .build();
    serviceDiscovery.registerService(serviceInstance);
  }

  @Override
  public void unregister(ServiceMeta serviceMeta) throws Exception {
    ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
        .<ServiceMeta>builder()
        .name(ServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getVersion(),
            serviceMeta.getGroup()))
        .address(serviceMeta.getServiceHost())
        .port(serviceMeta.getServicePort())
        .payload(serviceMeta)
        .build();
    serviceDiscovery.unregisterService(serviceInstance);
  }

  @Override
  public ServiceMeta lookup(String group, String serviceName,
      String serviceVersion, int invokerHashCode) throws Exception {
    Collection<ServiceInstance<ServiceMeta>> serviceInstances = serviceDiscovery
        .queryForInstances(ServiceHelper.buildServiceKey(serviceName, serviceVersion, group));
    ServiceInstance<ServiceMeta> instance = new ZKConsistentHashLoadBalancer()
        .select((List<ServiceInstance<ServiceMeta>>) serviceInstances, invokerHashCode);
    if (instance != null) {
      return instance.getPayload();
    }
    return null;
  }
}
