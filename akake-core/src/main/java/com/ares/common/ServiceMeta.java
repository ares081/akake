package com.ares.common;

import com.ares.common.constant.Constants;
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
public class ServiceMeta implements Serializable {

  @Serial
  private static final long serialVersionUID = -1025341288045851698L;

  private Class<?> serviceClass;
  private String serviceName;
  private String methodName;
  private Object[] params;
  private Class<?>[] paramTypes;
  private String version;
  @Builder.Default
  private String group = Constants.DEFAULT_GROUP;
  private String application;
  private String serviceHost;
  private Integer servicePort;
}
