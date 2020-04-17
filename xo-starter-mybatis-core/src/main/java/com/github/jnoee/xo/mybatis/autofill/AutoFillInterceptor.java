package com.github.jnoee.xo.mybatis.autofill;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
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
import com.github.jnoee.xo.utils.StringUtils;

/**
 * 自动填充拦截器。
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare",
    args = {Connection.class, Integer.class})})
public class AutoFillInterceptor implements Interceptor {
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    MappedStatement mappedStatement =
        (MappedStatement) BeanUtils.getField(statementHandler, "delegate.mappedStatement");
    SqlCommandType sqlType = mappedStatement.getSqlCommandType();
    if (sqlType != SqlCommandType.INSERT && sqlType != SqlCommandType.UPDATE) {
      return invocation.proceed();
    }
    Class<?> paramType = getParamType(statementHandler);
    if (paramType == Void.class) {
      return invocation.proceed();
    }
    EntityMeta entityMeta = EntityMetaManager.get(paramType);
    if (entityMeta != null) {
      if (sqlType == SqlCommandType.INSERT) {
        processInsert(statementHandler.getBoundSql(), entityMeta.getAutoInsertFieldMetas());
      }
      if (sqlType == SqlCommandType.UPDATE) {
        processUpdate(statementHandler.getBoundSql(), entityMeta.getAutoUpdateFieldMetas());
      }
    }
    return invocation.proceed();
  }

  /**
   * 处理新增时的自动填充。
   * 
   * @param boundSql BoundSql对象
   * @param fieldMetas 待自动填充的属性元数据列表
   */
  private void processInsert(BoundSql boundSql, List<FieldMeta> fieldMetas) {
    if (fieldMetas.isEmpty()) {
      return;
    }
    Map<String, String> params = new LinkedHashMap<>();
    fieldMetas.forEach(meta -> params.put(meta.getColumn(),
        "'" + AutoFillManager.getInstance().getVar(meta.getFillVar()) + "'"));

    String sql = boundSql.getSql();
    Pattern p = Pattern.compile("\\(([^)]*)\\)");
    Matcher m = p.matcher(sql);

    m.find();
    String fields = m.group(1);

    String replaceFields = StringUtils.join(params.keySet(), ",");
    replaceFields = fields + "," + replaceFields;
    sql = sql.replace(fields, replaceFields);

    m.find();
    String values = m.group(1);
    String replaceValues = StringUtils.join(params.values(), ",");
    replaceValues = values + "," + replaceValues;
    sql = sql.replace(values, replaceValues);

    BeanUtils.setField(boundSql, "sql", sql);
  }

  /**
   * 处理更新时的自动填充。
   * 
   * @param boundSql BoundSql对象
   * @param fieldMetas 待自动填充的属性元数据列表
   */
  private void processUpdate(BoundSql boundSql, List<FieldMeta> fieldMetas) {
    if (fieldMetas.isEmpty()) {
      return;
    }
    Map<String, String> params = new LinkedHashMap<>();
    fieldMetas.forEach(meta -> params.put(meta.getColumn(),
        "'" + AutoFillManager.getInstance().getVar(meta.getFillVar()) + "'"));

    String sql = boundSql.getSql();
    Pattern p = Pattern.compile("\\sSET\\s(.*?)\\sWHERE\\s", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(sql);

    m.find();
    String values = m.group(1);
    String replaceValues = StringUtils.mapToString(params, "=");
    replaceValues = values + "," + replaceValues;
    sql = sql.replace(values, replaceValues);

    BeanUtils.setField(boundSql, "sql", sql);
  }

  /**
   * 获取参数类型。
   * 
   * @param statementHandler StatementHandler对象
   * @return 返回参数类型。
   */
  private Class<?> getParamType(StatementHandler statementHandler) {
    Object param = statementHandler.getParameterHandler().getParameterObject();
    if (param == null) {
      return Void.class;
    }
    Class<?> paramType = param.getClass();
    if (ParamMap.class.isAssignableFrom(paramType)) {
      param = ((Map<?, ?>) param).get("param1");
      if (param == null) {
        return Void.class;
      }
      paramType = param.getClass();
    }
    return paramType;
  }
}
