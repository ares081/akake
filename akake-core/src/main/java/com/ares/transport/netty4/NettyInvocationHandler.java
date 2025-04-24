package com.ares.transport.netty4;

import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ares.common.config.ClientConfigProperties;
import com.ares.common.exception.RpcException;
import com.ares.proxy.AbstractRpcInvocationHandler;
import com.ares.transport.AbstractRpcClient;

public class NettyInvocationHandler extends AbstractRpcInvocationHandler {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public NettyInvocationHandler(ClientConfigProperties properties) {
    super(properties);
  }

  @Override
  protected AbstractRpcClient createClient(ClientConfigProperties properties) throws RpcException {
    try {
      return new NettyClient(properties);
    } catch (Exception e) {
      logger.error("create rpc client failed: {}", e);
      throw new RpcException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<T> clazz, ClientConfigProperties properties) throws Exception {
    return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
        new Class[] { clazz }, new NettyInvocationHandler(properties));
  }

}
