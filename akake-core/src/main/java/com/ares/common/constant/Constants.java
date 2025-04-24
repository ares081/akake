package com.ares.common.constant;

public class Constants {

  public static final byte[] MAGIC = { (byte) 0xAF, (byte) 0xBF };
  public static final byte HEADER_LEN = 20;
  public static final byte PROTOCOL_VERSION = 1;
  public static final int DEFAULT_RETRIES = 3;

  public static final int DEFAULT_PORT = 8099;

  public static final int DEFAULT_CONNECT_TIMEOUT = 1000;
  public static final int DEFAULT_READ_TIMEOUT = 3000;
  public static final int DEFAULT_IDLE_TIMEOUT = 1000 * 60;
  public static final int DEFAULT_MIN_WORKER_THREAD = Runtime.getRuntime().availableProcessors() * 2;
  public static final int DEFAULT_MAX_WORKER_THREAD = 200;

  public static final int DEFAULT_MAX_CONNECTION = 10000;
  public static final int DEFAULT_MAX_CONTENT_LENGTH = 1024 * 1024 * 10;

  public static final String DEFAULT_SERIALIZER = "hessian2";
  public static final String DEFAULT_PROTOCOL = "akake";
  public static final String DEFAULT_TRANSPORT = "netty4";
  public static final String DEFAULT_GROUP = "default";
  public static final String DEFAULT_SERVICE_VERSION = "utf-8";
}
