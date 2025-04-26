package com.ares.common.pool;

public interface ConnectionValidator<T> {
  boolean validate(T channel);

  void closeConnection(T channel);
}
