package com.github.jnoee.xo.mybatis.meta;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;
import org.springframework.core.annotation.AnnotationUtils;

import com.github.jnoee.xo.exception.SysException;
import com.github.jnoee.xo.mybatis.annotation.Column;
import com.github.jnoee.xo.mybatis.annotation.Id;
import com.github.jnoee.xo.mybatis.annotation.Many;
import com.github.jnoee.xo.mybatis.annotation.One;
import com.github.jnoee.xo.mybatis.autofill.AutoFillMode;
import com.github.jnoee.xo.mybatis.usertype.ArrayTypeHandler;
import com.github.jnoee.xo.mybatis.usertype.JsonTypeHandler;
import com.github.jnoee.xo.mybatis.usertype.ListTypeHandler;
import com.github.jnoee.xo.utils.BeanUtils;
import com.github.jnoee.xo.utils.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 字段元数据。
 */
@Getter
@Setter
public class FieldMeta {
  private String property;
  private String column;
  private Class<?> javaType;
  private JdbcType jdbcType = JdbcType.NULL;
  private Class<? extends TypeHandler<?>> typeHandler = UnknownTypeHandler.class;
  private TypeHandler<?> typeHandlerInstance;
  private Id id;
  private Column col;
  private One one;
  private Many many;
  private AutoFillMode fillMode = AutoFillMode.NONE;
  private String fillVar = "";

  public FieldMeta(Field field) {
    property = field.getName();
    column = field.getName();
    javaType = field.getType();
    typeHandler = getTypeHandler(field);
    processIdAnnotation(field);
    processColumnAnnotation(field);
    processOneAnnotation(field);
    processManyAnnotation(field);
  }

  public ResultMapping genResultMapping(Configuration configuration) {
    if (one != null) {
      String select = one.dao().getName() + "." + one.method();
      return new ResultMapping.Builder(configuration, property, column, javaType)
          .nestedQueryId(select).build();
    }
    if (many != null) {
      String select = many.dao().getName() + "." + many.method();
      return new ResultMapping.Builder(configuration, property, column, javaType)
          .nestedQueryId(select).build();
    }
    if (id != null || col != null) {
      if (typeHandlerInstance == null) {
        return new ResultMapping.Builder(configuration, property, column, javaType).build();
      } else {
        return new ResultMapping.Builder(configuration, property, column, typeHandlerInstance)
            .build();
      }
    }
    return null;
  }

  public String genEqualsCondition() {
    return genEqualsCondition(null);
  }

  public String genEqualsCondition(String prefix) {
    return column + " = " + genConditionValue(prefix);
  }

  public String genNotEqualsCondition() {
    return genNotEqualsCondition(null);
  }

  public String genNotEqualsCondition(String prefix) {
    return column + " != " + genConditionValue(prefix);
  }

  public String genConditionValue() {
    return genConditionValue(null);
  }

  public String genConditionValue(String prefix) {
    StringBuilder builder = new StringBuilder("#{");
    if (StringUtils.isNotBlank(prefix)) {
      builder.append(prefix);
      builder.append(".");
    }
    if (one == null) {
      builder.append(property);
    } else {
      builder.append(property + "." + one.property());
    }
    if (typeHandlerInstance != null) {
      builder.append(",typeHandler=");
      builder.append(typeHandlerInstance.getClass().getName());
    }
    builder.append("}");
    return builder.toString();
  }

  private void processIdAnnotation(Field field) {
    if (field.isAnnotationPresent(Id.class)) {
      id = AnnotationUtils.getAnnotation(field, Id.class);
      if (!id.column().isEmpty()) {
        column = id.column();
      }
    }
  }

  private void processManyAnnotation(Field field) {
    if (field.isAnnotationPresent(Many.class)) {
      many = AnnotationUtils.getAnnotation(field, Many.class);
      if (!many.column().isEmpty()) {
        column = many.column();
      } else {
        column = many.property();
      }
    }
  }

  private void processOneAnnotation(Field field) {
    if (field.isAnnotationPresent(One.class)) {
      one = AnnotationUtils.getAnnotation(field, One.class);
      if (!one.column().isEmpty()) {
        column = one.column();
      }
      fillMode = one.fillMode();
      fillVar = one.fillVar();
    }
  }

  private void processColumnAnnotation(Field field) {
    if (field.isAnnotationPresent(Column.class)) {
      col = AnnotationUtils.getAnnotation(field, Column.class);
      if (!col.name().isEmpty()) {
        column = col.name();
      }
      jdbcType = col.jdbcType();
      if (col.typeHandler() != UnknownTypeHandler.class) {
        typeHandler = col.typeHandler();
      }
      if (col.json()) {
        typeHandlerInstance = new JsonTypeHandler<>(javaType);
      }
      fillMode = col.fillMode();
      fillVar = col.fillVar();
    }
    if (typeHandler != UnknownTypeHandler.class && typeHandlerInstance == null) {
      try {
        typeHandlerInstance = typeHandler.newInstance();
      } catch (Exception e) {
        throw new SysException("生成TypeHandler实例时发生异常。", e);
      }
    }
  }

  private Class<? extends TypeHandler<?>> getTypeHandler(Field field) {
    // 虽然注册了自定义TypeHandler，但是通过 SqlProvider 生成的SQL语句，没有被自动解析
    // 所以这里自己判断类型并设置对应的TypeHandler
    if (String[].class.isAssignableFrom(field.getType())) {
      return ArrayTypeHandler.class;
    }
    if (List.class.isAssignableFrom(field.getType())
        && BeanUtils.getFieldType(field) == String.class) {
      return ListTypeHandler.class;
    }
    return UnknownTypeHandler.class;
  }
}
