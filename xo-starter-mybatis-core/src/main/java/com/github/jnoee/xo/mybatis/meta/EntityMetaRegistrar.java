package com.github.jnoee.xo.mybatis.meta;

import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;

import com.github.jnoee.xo.mybatis.annotation.Table;
import com.github.jnoee.xo.registrar.PackageScanRegistrar;

import lombok.extern.slf4j.Slf4j;

/**
 * 实体元数据注册器。
 */
@Slf4j
public class EntityMetaRegistrar implements PackageScanRegistrar {
  @Override
  public void registerBeanDefinitions(AnnotationMetadata metadata,
      BeanDefinitionRegistry registry) {
    List<Class<?>> entityClasses =
        findClassesByAnnotationClass(metadata, EntityScan.class, Table.class);
    entityClasses.forEach(entityClass -> {
      log.info("扫描到实体类[{}]", entityClass.getSimpleName());
      EntityMetaManager.add(entityClass);
    });
  }
}
