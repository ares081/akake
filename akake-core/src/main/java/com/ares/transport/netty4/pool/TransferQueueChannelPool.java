package com.ares.transport.netty4.pool;

import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

public class TransferQueueChannelPool implements ChannelPool {
  private final Bootstrap bootstrap;
  private final ChannelPoolHandler handler;
  private final ChannelHealthChecker healthCheck;
  private final ChannelPoolProperties properties;
  private final LinkedTransferQueue<PooledChannel> channelQueue;
  private final ScheduledExecutorService executor;
  private final AtomicInteger totalConnections;
  private final Queue<Promise<Channel>> pendingAcquires;
  private volatile boolean closed;

  public TransferQueueChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler,
      ChannelPoolProperties properties) {
    this.bootstrap = bootstrap.clone();
    this.handler = handler;
    this.properties = properties;
    this.healthCheck = ChannelHealthChecker.ACTIVE;
    this.channelQueue = new LinkedTransferQueue<>();
    this.executor = Executors.newSingleThreadScheduledExecutor();
    this.totalConnections = new AtomicInteger(0);
    this.pendingAcquires = new ConcurrentLinkedQueue<>();

    // 初始化最小连接
    initializeMinConnections();

    // 启动空闲连接检查
    startIdleConnectionChecker();
  }

  private void initializeMinConnections() {
    for (int i = 0; i < properties.getMinConnections(); i++) {
      createConnection().thenAccept(channel -> {
        if (channel != null) {
          channelQueue.offer(new PooledChannel(channel, Instant.now().toEpochMilli()));
        }
      });
    }
  }

  private void startIdleConnectionChecker() {
    executor.scheduleAtFixedRate(() -> {
      long now = System.currentTimeMillis();
      PooledChannel pooledChannel;
      while ((pooledChannel = channelQueue.poll()) != null) {
        if (now - pooledChannel.lastUsed > properties.getMaxIdleTime() * 1000L &&
            totalConnections.get() > properties.getMinConnections()) {

          pooledChannel.channel.close();
          totalConnections.decrementAndGet();
        } else {
          channelQueue.offer(pooledChannel);
        }
      }
    }, properties.getIdleCheckInterval(), properties.getIdleCheckInterval(), TimeUnit.SECONDS);
  }

  @Override
  public Future<Channel> acquire() {
    return acquire(bootstrap.config().group().next().newPromise());
  }

  @Override
  public Future<Channel> acquire(Promise<Channel> promise) {
    if (closed) {
      promise.setFailure(new IllegalStateException("Pool is closed"));
      return promise;
    }
    if (totalConnections.get() >= properties.getMaxConnections() &&
        pendingAcquires.size() >= properties.getMaxWaitingAcquires()) {
      promise.setFailure(new IllegalStateException("Too many pending acquires"));
      return promise;
    }
    acquireChannel(promise);
    return promise;
  }

  @SuppressWarnings("unchecked")
  private void acquireChannel(Promise<Channel> promise) {
    PooledChannel pooledChannel = channelQueue.poll();
    if (pooledChannel != null) {
      Channel channel = pooledChannel.channel;

      if (properties.isOnBorrow()) {
        healthCheck.isHealthy(channel).addListener(f -> {
          if (f.isSuccess() && ((Future<Boolean>) f).getNow()) {
            succeedAcquire(channel, promise);
          } else {
            channel.close();
            totalConnections.decrementAndGet();
            acquireChannel(promise);
          }
        });
      } else {
        succeedAcquire(channel, promise);
      }
      return;
    }
    if (totalConnections.get() < properties.getMaxConnections()) {
      createConnection().thenAccept(channel -> {
        if (channel != null) {
          succeedAcquire(channel, promise);
        } else {
          promise.setFailure(new Exception("Failed to create connection"));
        }
      });
    } else {
      if (pendingAcquires.size() < properties.getMaxWaitingAcquires()) {
        pendingAcquires.offer(promise);
        scheduleAcquireTimeout(promise);
      } else {
        promise.setFailure(new IllegalStateException("Too many pending acquires"));
      }
    }
  }

  private CompletableFuture<Channel> createConnection() {
    CompletableFuture<Channel> future = new CompletableFuture<>();
    if (totalConnections.incrementAndGet() > properties.getMaxConnections()) {
      totalConnections.decrementAndGet();
      future.complete(null);
      return future;
    }

    bootstrap.connect().addListener((ChannelFutureListener) f -> {
      if (f.isSuccess()) {
        Channel channel = f.channel();
        handler.channelCreated(channel);
        future.complete(channel);
      } else {
        totalConnections.decrementAndGet();
        future.complete(null);
      }
    });

    return future;
  }

  private void scheduleAcquireTimeout(Promise<Channel> promise) {
    executor.schedule(() -> {
      if (pendingAcquires.remove(promise)) {
        promise.setFailure(new TimeoutException("Acquire timed out"));
      }
    }, properties.getAcquireTimeout(), TimeUnit.MILLISECONDS);
  }

  @Override
  public Future<Void> release(Channel channel) {
    return release(channel, bootstrap.config().group().next().<Void>newPromise());
  }

  @Override
  @SuppressWarnings("unchecked")
  public Future<Void> release(Channel channel, Promise<Void> promise) {
    if (closed) {
      promise.setFailure(new IllegalStateException("Pool is closed"));
      return promise;
    }

    if (properties.isOnReturn()) {
      healthCheck.isHealthy(channel).addListener(f -> {
        if (f.isSuccess() && ((Future<Boolean>) f).getNow()) {
          releaseChannel(channel, promise);
        } else {
          channel.close();
          totalConnections.decrementAndGet();
          promise.setSuccess(null);
        }
      });
    } else {
      releaseChannel(channel, promise);
    }
    return promise;
  }

  private void releaseChannel(Channel channel, Promise<Void> promise) {
    PooledChannel pooledChannel = new PooledChannel(channel, Instant.now().toEpochMilli());
    Promise<Channel> pendingPromise = pendingAcquires.poll();
    if (pendingPromise != null) {
      succeedAcquire(channel, pendingPromise);
      promise.setSuccess(null);
    } else {
      if (channelQueue.offer(pooledChannel)) {
        promise.setSuccess(null);
      } else {
        channel.close();
        totalConnections.decrementAndGet();
        promise.setSuccess(null);
      }
    }
  }

  private void succeedAcquire(Channel channel, Promise<Channel> promise) {
    try {
      handler.channelAcquired(channel);
      promise.setSuccess(channel);
    } catch (Exception e) {
      promise.setFailure(e);
    }
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }
    closed = true;
    executor.shutdown();

    // 清空等待队列
    Promise<Channel> pending;
    while ((pending = pendingAcquires.poll()) != null) {
      pending.setFailure(new IllegalStateException("Pool is closed"));
    }

    // 关闭所有连接
    PooledChannel pooledChannel;
    while ((pooledChannel = channelQueue.poll()) != null) {
      pooledChannel.channel.close();
    }
  }
}
