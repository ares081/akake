package com.ares.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ares.common.ServiceMeta;
import com.ares.common.config.ClientConfigProperties;
import com.ares.common.exception.RpcException;
import com.ares.transport.AbstractRpcClient;

public abstract class AbstractRpcInvocationHandler implements InvocationHandler {

  private static final Logger logger = LoggerFactory.getLogger(AbstractRpcInvocationHandler.class);
  private final ClientConfigProperties properties;

  public AbstractRpcInvocationHandler(ClientConfigProperties properties) {
    this.properties = Objects.requireNonNull(properties, "properties cannot be null");
  }

  protected abstract AbstractRpcClient createClient(ClientConfigProperties properties)
      throws RpcException;

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(this, args);
    }
    AbstractRpcClient client = null;
    try {
      client = createClient(properties);
      ServiceMeta serviceMeta = lookupService(client);
      configureMethodInvocation(serviceMeta, method, args);
      if (!client.isActive()) {
        client.init(serviceMeta.getServiceHost(), serviceMeta.getServicePort());
      }
      return client.send(serviceMeta);
    } catch (RpcException e) {
      logger.error("RPC invocation failed for method: {}", method.getName(), e);
      throw e;
    }
  }

  private ServiceMeta lookupService(AbstractRpcClient client) throws RpcException {
    ServiceMeta serviceMeta = client.serviceLookup(
        properties.getGroup(),
        properties.getServiceRef(),
        properties.getServiceVersion());

    if (serviceMeta == null) {
      String errorMsg = String.format("Service not found: %s (group=%s, version=%s)",
          properties.getServiceRef(),
          properties.getGroup(),
          properties.getServiceVersion());
      logger.error(errorMsg);
      throw new RpcException(errorMsg);
    }
    return serviceMeta;
  }

  private void configureMethodInvocation(ServiceMeta serviceMeta, Method method, Object[] args) {
    serviceMeta.setApplication(properties.getApplication());
    serviceMeta.setMethodName(method.getName());
    serviceMeta.setParams(args);
    serviceMeta.setParamTypes(method.getParameterTypes());
  }
}