package com.ares.registry;

public class LoadBalanceFactory {

  private static LoadBalanceStrategy loadBalanceStrategy;

  public static LoadBalanceStrategy getLoadBalanceStrategy() {
    return loadBalanceStrategy;
  }

  public static void setLoadBalanceStrategy(LoadBalanceStrategy loadBalanceStrategy) {
    LoadBalanceFactory.loadBalanceStrategy = loadBalanceStrategy;
  }
}
