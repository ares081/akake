package com.ares.common;

import com.ares.common.exception.RpcException;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse implements Serializable {

  @Serial
  private static final long serialVersionUID = -13161773285029090L;
  private Long reqId;
  private Integer code;
  private Object data;
  private RpcException exception;
}
