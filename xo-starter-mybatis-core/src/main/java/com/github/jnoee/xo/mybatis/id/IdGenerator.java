package com.github.jnoee.xo.mybatis.id;

import java.io.Serializable;
import java.util.UUID;

import com.github.jnoee.xo.exception.SysException;
import com.github.jnoee.xo.utils.SpringUtils;

import io.shardingsphere.core.keygen.KeyGenerator;

public class IdGenerator {
  public static Serializable gen(IdType type) {
    switch (type) {
      case UUID:
        return UUID.randomUUID().toString();
      case SNOWFLAKE:
        return getKeyGenerator().generateKey();
      default:
        break;
    }
    return null;
  }

  private static KeyGenerator getKeyGenerator() {
    KeyGenerator keyGenerator = SpringUtils.getBean(KeyGenerator.class);
    if (keyGenerator == null) {
      throw new SysException("未引入支持雪花算法生成ID的模块。");
    }
    return keyGenerator;
  }

  private IdGenerator() {}
}
