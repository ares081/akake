package com.ares.transport;

public abstract class AbstractRpcChannel implements RpcChannel {
  protected volatile boolean active;

  @Override
  public boolean isActive() {
    return active;
  }

  protected void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Initialize the channel
   */
  protected abstract void init(String host, Integer port);
}
