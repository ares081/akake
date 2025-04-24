package com.ares.common.exception;

public class CompressException extends RpcException {

  public CompressException(String message) {
    super(message);
  }

  public CompressException(Throwable cause) {
    super(cause);
  }

  public CompressException(String message, Throwable cause) {
    super(message, cause);
  }
}
