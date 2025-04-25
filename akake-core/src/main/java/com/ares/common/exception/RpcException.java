package com.ares.common.exception;

public class RpcException extends Exception {

  public RpcException(String msg) {
    super(msg);
  }

  public RpcException(Throwable e) {
    super(e);
  }

  public RpcException(String msg, Throwable e) {
    super(msg, e);
  }
}
