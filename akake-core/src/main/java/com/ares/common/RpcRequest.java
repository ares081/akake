package com.ares.common;

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
public class RpcRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = -5145679540746920882L;

  private Long reqId;
  private String methodName;
  private String serviceName;
  private String serviceVersion;
  private String group;
  private Object[] params;
  private Class<?>[] paramTypes;
}
