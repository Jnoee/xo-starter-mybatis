package com.github.jnoee.xo.mybatis.meta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

import com.github.jnoee.xo.mybatis.annotation.Table;
import com.github.jnoee.xo.mybatis.autofill.AutoFillMode;
import com.github.jnoee.xo.utils.BeanUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 实体元数据。
 */
@Getter
@Setter
public class EntityMeta {
  /** 表名称 */
  private String tableName;
  /** 实体类 */
  private Class<?> entityClass;
  /** ID属性元数据 */
  private FieldMeta idMeta;
  /** 属性元数据 */
  private Map<String, FieldMeta> fieldMetas = new LinkedHashMap<>();
  /** 新增时自动填充的属性元数据列表 */
  private List<FieldMeta> autoInsertFieldMetas = new ArrayList<>();
  /** 更新时自动填充的属性元数据列表 */
  private List<FieldMeta> autoUpdateFieldMetas = new ArrayList<>();
  private List<String> propertyNames = new ArrayList<>();
  private List<String> columnNames = new ArrayList<>();

  public EntityMeta(Class<?> entityClass) {
    this.entityClass = entityClass;
    tableName = entityClass.getSimpleName().toLowerCase();
    Table anno = AnnotationUtils.getAnnotation(entityClass, Table.class);
    if (!anno.name().isEmpty()) {
      tableName = anno.name();
    }
    getFields().forEach(this::initField);
  }

  public ResultMap genResultMap(Configuration configuration) {
    List<ResultMapping> resultMappings = new ArrayList<>();
    fieldMetas.values().forEach(fieldMeta -> {
      ResultMapping resultMapping = fieldMeta.genResultMapping(configuration);
      if (resultMapping != null) {
        resultMappings.add(resultMapping);
      }
    });
    String id = entityClass.getName() + ".map";
    return new ResultMap.Builder(configuration, id, entityClass, resultMappings).build();
  }

  public FieldMeta getFieldMeta(String propertyName) {
    return fieldMetas.get(propertyName);
  }

  public List<String> genEqualsConditions(String... fieldNames) {
    return genEqualsConditions(null, fieldNames);
  }

  public List<String> genEqualsConditions(String prefix, String... fieldNames) {
    List<String> conditions = new ArrayList<>();
    Arrays.stream(fieldNames)
        .forEach(fieldName -> conditions.add(fieldMetas.get(fieldName).genEqualsCondition(prefix)));
    return conditions;
  }

  public List<String> genNotEqualsConditions(String... fieldNames) {
    return genNotEqualsConditions(null, fieldNames);
  }

  public List<String> genNotEqualsConditions(String prefix, String... fieldNames) {
    List<String> conditions = new ArrayList<>();
    Arrays.stream(fieldNames).forEach(
        fieldName -> conditions.add(fieldMetas.get(fieldName).genNotEqualsCondition(prefix)));
    return conditions;
  }

  /**
   * 初始化单个Field。
   * 
   * @param field Field
   */
  private void initField(Field field) {
    FieldMeta fieldMeta = new FieldMeta(field);
    if (fieldMeta.getId() != null) {
      idMeta = fieldMeta;
    }
    fieldMetas.put(fieldMeta.getProperty(), fieldMeta);
    AutoFillMode autoFillMode = fieldMeta.getFillMode();
    if (fieldMeta.getMany() == null && autoFillMode == AutoFillMode.NONE) {
      propertyNames.add(fieldMeta.genConditionValue());
      columnNames.add(fieldMeta.getColumn());
    }
    switch (autoFillMode) {
      case INSERT:
        autoInsertFieldMetas.add(fieldMeta);
        break;
      case UPDATE:
        autoUpdateFieldMetas.add(fieldMeta);
        break;
      case INSERT_UPDATE:
        autoInsertFieldMetas.add(fieldMeta);
        autoUpdateFieldMetas.add(fieldMeta);
        break;
      default:
        break;
    }
  }

  /**
   * 获取Field列表。
   * 
   * @return 返回Field列表。
   */
  private List<Field> getFields() {
    Map<String, Field> fields = BeanUtils.getDeclaredFields(entityClass);
    return new ArrayList<>(fields.values());
  }
}
