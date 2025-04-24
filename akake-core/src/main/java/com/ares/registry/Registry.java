package com.ares.registry;

import com.ares.common.ServiceMeta;

public interface Registry {
  void register(ServiceMeta serviceMeta) throws Exception;

  void unregister(ServiceMeta serviceMeta) throws Exception;

  ServiceMeta lookup(String group, String serviceName, String serviceVersion,
      int invokerHashCode) throws Exception;
}
