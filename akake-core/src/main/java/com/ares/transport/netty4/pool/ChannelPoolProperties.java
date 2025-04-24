package com.ares.transport.netty4.pool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ChannelPoolProperties {

  private int maxConnections; // 最大连接数
  private int minConnections; // 最小空闲连接数
  private int maxIdleTime; // 最大空闲时间(秒)
  private int connectTimeout; // 连接超时时间(毫秒)
  private int acquireTimeout; // 获取连接超时时间(毫秒)
  private boolean isOnBorrow; // 获取连接时是否测试
  private boolean isOnReturn; // 返回连接时是否测试
  private int maxWaitingAcquires; // 最大等待获取连接数
  private int idleCheckInterval; // 空闲连接检查间隔(秒)
}
