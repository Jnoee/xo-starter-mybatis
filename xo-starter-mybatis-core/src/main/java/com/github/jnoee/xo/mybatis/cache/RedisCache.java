package com.github.jnoee.xo.mybatis.cache;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.jnoee.xo.utils.SpringUtils;

public class RedisCache implements Cache {
  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
  private final String id;

  public RedisCache(String id) {
    if (id == null) {
      throw new IllegalArgumentException("缓存实例必须指定ID。");
    }
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void putObject(Object key, Object value) {
    getRedisTemplate().opsForHash().put(id, key.toString(), value);
  }

  @Override
  public Object getObject(Object key) {
    return getRedisTemplate().opsForHash().get(id, key.toString());
  }

  @Override
  public Object removeObject(Object key) {
    getRedisTemplate().opsForHash().delete(id, key.toString());
    return null;
  }

  @Override
  public void clear() {
    getRedisTemplate().delete(id);
  }

  @Override
  public int getSize() {
    return getRedisTemplate().opsForHash().size(id).intValue();
  }

  @Override
  public ReadWriteLock getReadWriteLock() {
    return readWriteLock;
  }

  private RedisTemplate<Object, Object> getRedisTemplate() {
    return SpringUtils.getBean("redisTemplate");
  }
}
