package com.ares.transport.netty4;

import io.netty.util.concurrent.Promise;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NettyFuture<T> {

  private Promise<T> promise;
  private long timeout;

  public NettyFuture(Promise<T> promise, long timeout) {
    this.promise = promise;
    this.timeout = timeout;
  }
}