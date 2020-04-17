package com.github.jnoee.xo.mybatis.id;

import java.io.Serializable;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import com.github.jnoee.xo.mybatis.meta.EntityMeta;
import com.github.jnoee.xo.mybatis.meta.EntityMetaManager;
import com.github.jnoee.xo.mybatis.meta.FieldMeta;
import com.github.jnoee.xo.utils.BeanUtils;

@Intercepts({@Signature(type = Executor.class, method = "update",
    args = {MappedStatement.class, Object.class}),})
public class IdInterceptor implements Interceptor {
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    Object[] args = invocation.getArgs();
    MappedStatement mappedStatement = (MappedStatement) args[0];
    if (mappedStatement.getSqlCommandType() != SqlCommandType.INSERT) {
      return invocation.proceed();
    }
    Object entity = args[1];
    EntityMeta entityMeta = EntityMetaManager.get(entity.getClass());
    if (entityMeta == null) {
      return invocation.proceed();
    }
    FieldMeta idMeta = entityMeta.getIdMeta();
    Serializable idValue = IdGenerator.gen(idMeta.getId().value());
    if (idValue == null) {
      return invocation.proceed();
    }
    BeanUtils.setField(entity, entityMeta.getIdMeta().getProperty(), idValue);
    return invocation.proceed();
  }
}
