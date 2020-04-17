package com.github.jnoee.xo.mybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.ibatis.mapping.FetchType;

/**
 * 一对多注解。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Many {
  /** 属性名 */
  String property() default "id";

  /** 字段名 */
  String column() default "";

  /** 执行select的DAO类 */
  Class<?> dao();

  /** 执行select的方法名 */
  String method();

  /** 懒加载模式 */
  FetchType fetchType() default FetchType.DEFAULT;
}
