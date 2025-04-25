package com.ares.transport;

import com.ares.common.exception.RpcException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractRpcChannel implements RpcChannel {

  protected final AtomicBoolean active = new AtomicBoolean(false);
  protected final AtomicBoolean initialized = new AtomicBoolean(false);

  @Override
  public boolean isActive() {
    return active.get();
  }

  /**
   * Initialize the channel
   */
  protected abstract void init(String host, Integer port) throws RpcException;
}
