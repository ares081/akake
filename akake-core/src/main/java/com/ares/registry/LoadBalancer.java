package com.ares.registry;

import java.util.List;

public interface LoadBalancer<T> {

  T select(List<T> servers, int hashCode);

}
