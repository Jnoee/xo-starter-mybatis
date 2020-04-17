package com.github.jnoee.xo.mybatis.meta;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * 实体元数据管理组件。
 */
public class EntityMetaManager {
  @Getter
  private static Map<Class<?>, EntityMeta> metas = new HashMap<>();

  public static EntityMeta get(Class<?> entityClass) {
    return metas.get(entityClass);
  }

  public static void add(Class<?> entityClass) {
    metas.put(entityClass, new EntityMeta(entityClass));
  }

  private EntityMetaManager() {}
}
