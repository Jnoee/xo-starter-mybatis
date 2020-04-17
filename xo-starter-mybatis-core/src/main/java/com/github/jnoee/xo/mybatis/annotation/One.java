package com.github.jnoee.xo.mybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.ibatis.mapping.FetchType;

import com.github.jnoee.xo.mybatis.autofill.AutoFillMode;

/**
 * 一对一注解。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface One {
  /** 属性名 */
  String property() default "id";

  /** 字段名 */
  String column() default "";

  /** 执行select的DAO类 */
  Class<?> dao();

  /** 执行select的方法名 */
  String method() default "get";

  FetchType fetchType() default FetchType.DEFAULT;

  /** 自动填充模式 */
  AutoFillMode fillMode() default AutoFillMode.NONE;

  /** 自动填充变量 */
  String fillVar() default "";
}
