package com.ares.transport.netty4;

import com.ares.common.config.ClientConfigProperties;
import com.ares.common.exception.RpcException;
import com.ares.proxy.AbstractRpcInvocationHandler;
import com.ares.transport.AbstractRpcClient;
import java.lang.reflect.Proxy;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyInvocationHandler extends AbstractRpcInvocationHandler {

  private static final Logger logger = LoggerFactory.getLogger(NettyInvocationHandler.class);

  public NettyInvocationHandler(ClientConfigProperties properties) {
    super(Objects.requireNonNull(properties, "properties cannot be null"));
  }

  @Override
  protected AbstractRpcClient createClient(ClientConfigProperties properties) throws RpcException {
    try {
      AbstractRpcClient client = new NettyClient(properties);
      logger.debug("Created new Netty RPC client");
      return client;
    } catch (Exception e) {
      logger.error("Failed to create Netty RPC client", e);
      throw new RpcException("Failed to create Netty RPC client", e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<T> clazz, ClientConfigProperties properties)
      throws RpcException {
    try {
      // Validate parameters
      Objects.requireNonNull(clazz, "interface class cannot be null");
      Objects.requireNonNull(properties, "properties cannot be null");

      // Validate interface
      if (!clazz.isInterface()) {
        throw new IllegalArgumentException(clazz.getName() + " is not an interface");
      }

      // Create proxy instance
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      T proxy = (T) Proxy.newProxyInstance(
          classLoader,
          new Class<?>[]{clazz},
          new NettyInvocationHandler(properties));

      logger.debug("Created new proxy instance for interface: {}", clazz.getName());
      return proxy;

    } catch (Exception e) {
      logger.error("Failed to create proxy instance for interface: {}", clazz.getName(), e);
      throw new RpcException("Failed to create proxy instance", e);
    }
  }

}
