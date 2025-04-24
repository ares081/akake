package com.ares.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ares.common.ServiceMeta;
import com.ares.common.config.ClientConfigProperties;
import com.ares.common.exception.RpcException;
import com.ares.transport.AbstractRpcClient;

public abstract class AbstractRpcInvocationHandler implements InvocationHandler {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final ClientConfigProperties properties;

  public AbstractRpcInvocationHandler(ClientConfigProperties properties) {
    this.properties = properties;
  }

  protected abstract AbstractRpcClient createClient(ClientConfigProperties properties) throws RpcException;

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // 创建 Client
    AbstractRpcClient client = createClient(properties);
    ServiceMeta serviceMeta = client.servieLookup(properties.getGroup(), properties.getServiceName(),
        properties.getServiceVersion());
    if (serviceMeta == null) {
      logger.error("service not found: " + properties.getServiceName());
      throw new RpcException("service not found: " + properties.getServiceName());
    }
    serviceMeta.setApplication(properties.getApplication());
    serviceMeta.setMethodName(method.getName());
    serviceMeta.setParams(args);
    serviceMeta.setParamTypes(method.getParameterTypes());
    // 初始化 Channel
    client.init(serviceMeta.getServiceHost(), serviceMeta.getServicePort());
    return client.send(serviceMeta);
  }
}
