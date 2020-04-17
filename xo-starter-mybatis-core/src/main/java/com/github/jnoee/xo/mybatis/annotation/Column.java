package com.github.jnoee.xo.mybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;
import org.springframework.core.annotation.AliasFor;

import com.github.jnoee.xo.mybatis.autofill.AutoFillMode;

/**
 * 列注解。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
  @AliasFor("value")
  String name() default "";

  @AliasFor("name")
  String value() default "";

  JdbcType jdbcType() default JdbcType.NULL;

  Class<? extends TypeHandler<?>> typeHandler() default UnknownTypeHandler.class;

  /** 是否保存为JSON格式 */
  boolean json() default false;

  /** 自动填充模式 */
  AutoFillMode fillMode() default AutoFillMode.NONE;

  /** 自动填充变量 */
  String fillVar() default "";
}
