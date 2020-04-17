package com.github.jnoee.xo.mybatis.dao;

import java.sql.Statement;
import java.util.Collections;

import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import com.github.jnoee.xo.mybatis.meta.EntityMeta;
import com.github.jnoee.xo.mybatis.meta.EntityMetaManager;
import com.github.jnoee.xo.utils.BeanUtils;
import com.github.jnoee.xo.utils.CollectionUtils;

/**
 * 通用ResultMap拦截器。
 */
@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets",
    args = {Statement.class})})
public class ResultMapInterceptor implements Interceptor {
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    DefaultResultSetHandler handler = (DefaultResultSetHandler) invocation.getTarget();
    MappedStatement ms = (MappedStatement) BeanUtils.getField(handler, "mappedStatement");
    String resource = ms.getResource();
    // 如果是XML配置的资源，不处理。
    if (resource.contains(".xml")) {
      return invocation.proceed();
    }
    // 如果已经配置了ResultMap，不处理。
    ResultMap resultMap = ms.getResultMaps().iterator().next();
    if (!CollectionUtils.isEmpty(resultMap.getResultMappings())) {
      return invocation.proceed();
    }
    // 如果ResultMap类型不是实体类，不处理。
    Class<?> mapType = resultMap.getType();
    EntityMeta entityMeta = EntityMetaManager.get(mapType);
    if (entityMeta == null) {
      return invocation.proceed();
    }

    ResultMap autoResultMap = entityMeta.genResultMap(ms.getConfiguration());
    BeanUtils.setField(ms, "resultMaps", Collections.singletonList(autoResultMap));

    return invocation.proceed();
  }
}
