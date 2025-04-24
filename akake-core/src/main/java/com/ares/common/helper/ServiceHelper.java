package com.ares.common.helper;

public class ServiceHelper {
  public static String buildServiceKey(String serviceName, String serviceVersion, String group) {
    return String.join("#", group, serviceName, serviceVersion);
  }
}
