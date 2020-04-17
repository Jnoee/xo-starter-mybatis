package com.github.jnoee.xo.mybatis.usertype;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jnoee.xo.utils.StringUtils;

/**
 * Json转换器。
 * 
 * @param <T> 对象类型
 */
public class JsonTypeHandler<T> extends BaseTypeHandler<T> {
  private ObjectMapper mapper = new ObjectMapper();
  private Class<T> type;

  public JsonTypeHandler(Class<T> type) {
    this.type = type;
  }

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, this.toJson(parameter));
  }

  @Override
  public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return this.toObject(rs.getString(columnName));
  }

  @Override
  public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return this.toObject(rs.getString(columnIndex));
  }

  @Override
  public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return this.toObject(cs.getString(columnIndex));
  }

  private String toJson(T object) throws SQLException {
    try {
      return mapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new SQLException("转换目标对象为Json时发生异常。", e);
    }
  }

  private T toObject(String content) throws SQLException {
    if (StringUtils.isNotBlank(content)) {
      try {
        return mapper.readValue(content, type);
      } catch (Exception e) {
        throw new SQLException("转换目标对象为Json时发生异常。", e);
      }
    } else {
      return null;
    }
  }
}
