package com.github.jnoee.xo.mybatis.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.jdbc.SQL;

import com.github.jnoee.xo.exception.SysException;
import com.github.jnoee.xo.mybatis.meta.EntityMeta;
import com.github.jnoee.xo.mybatis.meta.EntityMetaManager;
import com.github.jnoee.xo.utils.CollectionUtils;
import com.github.jnoee.xo.utils.DateUtils;
import com.github.jnoee.xo.utils.StringUtils;

import lombok.Getter;

/**
 * 查询构造器。
 *
 * @param <E> 实体类型
 */
@Getter
public class Criteria<E> {
  private Class<E> entityClass;
  private EntityMeta entityMeta;
  private List<String> conditions = new ArrayList<>();
  private List<String> orderBys = new ArrayList<>();

  public Criteria(Class<E> entityClass) {
    this.entityClass = entityClass;
    entityMeta = EntityMetaManager.get(entityClass);
  }

  public SQL genSql() {
    return new SQL().SELECT("*").FROM(entityMeta.getTableName())
        .WHERE(conditions.toArray(new String[0])).ORDER_BY(orderBys.toArray(new String[0]));
  }

  public void addCondition(String condition) {
    if (StringUtils.isNotBlank(condition)) {
      conditions.add(condition);
    }
  }

  public void eq(String propertyName, Object value) {
    addCondition(getColumn(propertyName) + " = " + wrapValue(value));
  }

  public void notEq(String propertyName, Object value) {
    addCondition(getColumn(propertyName) + " != " + wrapValue(value));
  }

  public void isNull(String propertyName) {
    addCondition(getColumn(propertyName) + " is null");
  }

  public void isNotNull(String propertyName) {
    addCondition(getColumn(propertyName) + " not null");
  }

  public void like(String propertyNames, String value) {
    if (StringUtils.isNotBlank(propertyNames) && StringUtils.isNotBlank(value)) {
      if (value.indexOf('%') < 0) {
        value = "%" + value + "%";
      }
      if (propertyNames.contains(",")) {
        List<String> likeConditions = new ArrayList<>();
        for (String propertyName : propertyNames.split(",")) {
          likeConditions.add(getColumn(propertyName.trim()) + " like " + wrapValue(value));
        }
        addCondition("(" + StringUtils.join(likeConditions, " or ") + ")");
      } else {
        addCondition(getColumn(propertyNames) + " like " + wrapValue(value));
      }
    }
  }

  public void in(String propertyName, Collection<?> values) {
    if (CollectionUtils.isEmpty(values)) {
      throw new SysException("设置in查询条件不允许为空。");
    }
    List<String> ins = new ArrayList<>();
    values.forEach(value -> ins.add(wrapValue(value)));
    String condition = StringUtils.join(ins, " ,");
    condition = getColumn(propertyName) + " in (" + condition + ")";
    addCondition(condition);

  }

  public void notIn(String propertyName, Collection<?> values) {
    if (CollectionUtils.isEmpty(values)) {
      throw new SysException("设置not in查询条件不允许为空。");
    }
    List<String> ins = new ArrayList<>();
    values.forEach(value -> ins.add(wrapValue(value)));
    String condition = StringUtils.join(ins, " ,");
    condition = getColumn(propertyName) + " not in (" + condition + ")";
    addCondition(condition);
  }

  public void le(String propertyName, Date value) {
    addCondition(getColumn(propertyName) + " <= " + wrapValue(value));
  }

  public void le(String propertyName, Number value) {
    addCondition(getColumn(propertyName) + " <= " + wrapValue(value));
  }

  public void lt(String propertyName, Date value) {
    addCondition(getColumn(propertyName) + " < " + wrapValue(value));
  }

  public void lt(String propertyName, Number value) {
    addCondition(getColumn(propertyName) + " < " + wrapValue(value));
  }

  public void ge(String propertyName, Date value) {
    addCondition(getColumn(propertyName) + " >= " + wrapValue(value));
  }

  public void ge(String propertyName, Number value) {
    addCondition(getColumn(propertyName) + " >= " + wrapValue(value));
  }

  public void gt(String propertyName, Date value) {
    addCondition(getColumn(propertyName) + " > " + wrapValue(value));
  }

  public void gt(String propertyName, Number value) {
    addCondition(getColumn(propertyName) + " > " + wrapValue(value));
  }

  public void between(String propertyName, Number lo, Number go) {
    ge(propertyName, lo);
    le(propertyName, go);
  }

  public void between(String propertyName, Date startDate, Date endDate) {
    ge(propertyName, startDate);
    le(propertyName, endDate);
  }

  public void orderBy(String orderBy) {
    if (StringUtils.isNotBlank(orderBy)) {
      orderBys.add(orderBy);
    }
  }

  public void asc(String propertyName) {
    orderBy(getColumn(propertyName) + " ASC");
  }

  public void desc(String propertyName) {
    orderBy(getColumn(propertyName) + " DESC");
  }

  private String getColumn(String propertyName) {
    return entityMeta.getFieldMeta(propertyName).getColumn();
  }

  private String wrapValue(Object value) {
    String wrap = value.toString();
    if (value instanceof String && !wrap.contains("#")) {
      wrap = "'" + wrap + "'";
    }
    if (value instanceof Date) {
      Date date = (Date) value;
      wrap = DateUtils.format(date, DateUtils.SECOND);
      wrap = "'" + wrap + "'";
    }
    return wrap;
  }
}
