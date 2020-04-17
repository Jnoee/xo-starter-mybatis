package com.github.jnoee.xo.mybatis.cache;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.ibatis.cache.Cache;
import org.redisson.api.RedissonClient;

import com.github.jnoee.xo.utils.SpringUtils;

public class RedissonCache implements Cache {
  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
  private final String id;

  public RedissonCache(String id) {
    if (id == null) {
      throw new IllegalArgumentException("Cache instances require an ID");
    }
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void putObject(Object key, Object value) {
    getRedissonClient().getMap(id).put(key.toString(), value);
  }

  @Override
  public Object getObject(Object key) {
    return getRedissonClient().getMap(id).get(key.toString());
  }

  @Override
  public Object removeObject(Object key) {
    getRedissonClient().getMap(id).remove(key.toString());
    return null;
  }

  @Override
  public void clear() {
    getRedissonClient().getMap(id).clear();
  }

  @Override
  public int getSize() {
    return getRedissonClient().getMap(id).size();
  }

  @Override
  public ReadWriteLock getReadWriteLock() {
    return readWriteLock;
  }

  private RedissonClient getRedissonClient() {
    return SpringUtils.getBean("redisson");
  }
}
