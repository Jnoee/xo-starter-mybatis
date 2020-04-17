package com.github.jnoee.xo.mybatis.dao;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.core.ResolvableType;

import com.github.jnoee.xo.exception.SysException;
import com.github.jnoee.xo.mybatis.meta.EntityMeta;
import com.github.jnoee.xo.mybatis.meta.EntityMetaManager;
import com.github.jnoee.xo.mybatis.meta.FieldMeta;
import com.github.jnoee.xo.utils.BeanUtils;
import com.github.jnoee.xo.utils.CollectionUtils;

/**
 * 通用DAO的SQL语句产生器。
 */
public class DaoProvider {
  private static Map<Class<?>, Class<?>> entityClassMap = new HashMap<>();

  public static String get(ProviderContext context) {
    Criteria<?> criteria = genCriteria(context);
    criteria.eq(criteria.getEntityMeta().getIdMeta().getProperty(), "#{id}");
    return criteria.genSql().toString();
  }

  public static String getBy(Criteria<?> criteria) {
    return criteria.genSql().toString();
  }

  public static String getAll(ProviderContext context, String orderBy) {
    Criteria<?> criteria = genCriteria(context);
    criteria.orderBy(orderBy);
    return criteria.genSql().toString();
  }

  public static String findBy(@Param("criteria") Criteria<?> criteria) {
    return criteria.genSql().toString();
  }

  public static String add(ProviderContext context) {
    EntityMeta meta = getEntityMeta(context);
    String[] columns = meta.getColumnNames().toArray(new String[0]);
    SQL sql = new SQL().INSERT_INTO(meta.getTableName()).INTO_COLUMNS(columns);
    meta.getPropertyNames().forEach(property -> sql.INTO_VALUES(property));
    return sql.toString();
  }

  public static String update(ProviderContext context, @Param("fieldNames") String... fieldNames) {
    EntityMeta meta = getEntityMeta(context);
    SQL sql = new SQL().UPDATE(meta.getTableName());
    if (CollectionUtils.isNotEmpty(fieldNames)) {
      List<String> conditions = meta.genEqualsConditions("entity", fieldNames);
      sql.SET(conditions.toArray(new String[0]));
    }
    FieldMeta idMeta = meta.getIdMeta();
    sql.WHERE(idMeta.genEqualsCondition("entity"));
    return sql.toString();
  }

  public static String deleteBy(Criteria<?> criteria) {
    SQL sql = criteria.genSql();
    sql.DELETE_FROM("");
    return sql.toString();
  }

  @SuppressWarnings("unchecked")
  public static String count(Criteria<?> criteria) {
    SQL sql = criteria.genSql();
    // 反射移除已有的select条件
    List<String> select =
        (List<String>) BeanUtils.getField(BeanUtils.getField(sql, "sql"), "select");
    select.clear();
    sql.SELECT("count(*)");
    return sql.toString();
  }

  public static String isExist(ProviderContext context, @Param("entity") Object entity,
      @Param("fieldNames") String... fieldNames) {
    EntityMeta meta = getEntityMeta(context);
    SQL sql = new SQL().SELECT("count(*)").FROM(meta.getTableName());
    if (CollectionUtils.isNotEmpty(fieldNames)) {
      List<String> conditions = meta.genEqualsConditions("entity", fieldNames);
      FieldMeta idMeta = meta.getIdMeta();
      Object id = BeanUtils.getField(entity, idMeta.getProperty());
      if (id != null) {
        conditions.add(idMeta.genNotEqualsCondition("entity"));
      }
      sql.WHERE(conditions.toArray(new String[0]));
    }
    return sql.toString();
  }

  public static Class<?> getEntityClass(Class<?> daoClass) {
    return entityClassMap.computeIfAbsent(daoClass, key -> {
      for (Type parent : key.getGenericInterfaces()) {
        ResolvableType parentType = ResolvableType.forType(parent);
        if (parentType.getRawClass() == Dao.class) {
          return parentType.getGeneric(0).getRawClass();
        }
      }
      throw new SysException("获取实体类失败，对应DAO类为[" + key.getName() + "]。");
    });
  }

  private static Criteria<?> genCriteria(ProviderContext context) {
    return new Criteria<>(getEntityClass(context.getMapperType()));
  }

  private static EntityMeta getEntityMeta(ProviderContext context) {
    Class<?> entityClass = getEntityClass(context.getMapperType());
    return EntityMetaManager.get(entityClass);
  }

  private DaoProvider() {}
}
