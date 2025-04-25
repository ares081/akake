package com.ares.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RpcExporter {

  String serviceName() default "";

  Class<?> serviceInterface() default Object.class;

  String serviceGroup() default "default";

  String serviceVersion() default "1.0.0";

  String servicePot() default "8998";

}
