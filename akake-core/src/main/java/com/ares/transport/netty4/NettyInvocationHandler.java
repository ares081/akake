package com.ares.transport.netty4;

import java.lang.reflect.Proxy;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ares.common.config.ClientConfigProperties;
import com.ares.common.exception.RpcException;
import com.ares.proxy.AbstractRpcInvocationHandler;
import com.ares.transport.AbstractRpcClient;

public class NettyInvocationHandler extends AbstractRpcInvocationHandler {

  private static final Logger logger = LoggerFactory.getLogger(NettyInvocationHandler.class);
  private static volatile NettyClient nettyClient;

  public NettyInvocationHandler(ClientConfigProperties properties) {
    super(Objects.requireNonNull(properties, "properties cannot be null"));
  }

  @Override
  protected AbstractRpcClient createClient(ClientConfigProperties properties) throws RpcException {
    if (nettyClient == null) {
      synchronized (this) {
        if (nettyClient == null) {
          try {
            nettyClient = new NettyClient(properties);
          } catch (Exception e) {
            throw new RuntimeException("Failed to create NettyClient", e);
          }
        }
      }
    }
    return nettyClient;
  }

  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<T> clazz, ClientConfigProperties properties)
      throws RpcException {
    try {
      if (!clazz.isInterface()) {
        throw new IllegalArgumentException(clazz.getName() + " is not an interface");
      }
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      T proxy = (T) Proxy.newProxyInstance(
          classLoader,
          new Class<?>[] { clazz },
          new NettyInvocationHandler(properties));
      logger.debug("Created new proxy instance for interface: {}", clazz.getName());
      return proxy;
    } catch (Exception e) {
      logger.error("Failed to create proxy instance for interface: {}", clazz.getName(), e);
      throw new RpcException("Failed to create proxy instance", e);
    }
  }

}
