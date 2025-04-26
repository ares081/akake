package com.ares.common.pool;

public interface ConnectionValidator<T> {
  boolean validate(T connection);

  void closeConnection(T connection);
}
