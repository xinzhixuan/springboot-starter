package com.zzkj.springboot.starter.metric;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * api监控注解
 *
 * @author xinzhixuan
 * @version V1.0
 * @date 2017/12/11 16:49
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Metric {
    String value() default "";
}
