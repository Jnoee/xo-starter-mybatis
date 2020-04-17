package com.github.jnoee.xo.mybatis.dao;

import java.io.Serializable;
import java.util.List;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import com.github.jnoee.xo.model.Page;
import com.github.jnoee.xo.model.PageQuery;
import com.github.jnoee.xo.mybatis.utils.PageUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 通用DAO接口。
 *
 * @param <E> 实体类型
 */
public interface Dao<E> {
  /**
   * 创建查询条件构造器。
   * 
   * @return 返回查询条件构造器。
   */
  default Criteria<E> createCriteria() {
    Class<?> daoClass = (Class<?>) getClass().getGenericInterfaces()[0];
    @SuppressWarnings("unchecked")
    Class<E> entityClass = (Class<E>) DaoProvider.getEntityClass(daoClass);
    return new Criteria<>(entityClass);
  }

  /**
   * 获取指定ID的实体。
   * 
   * @param id ID
   * @return 返回指定的实体。
   */
  @SelectProvider(type = DaoProvider.class, method = "get")
  E get(Serializable id);

  /**
   * 获取满足唯一条件的实体。
   * 
   * @param name 属性名
   * @param value 属性值
   * @return 返回指定的实体。
   */
  default E getBy(String name, Object value) {
    Criteria<E> criteria = createCriteria();
    criteria.eq(name, value);
    return getBy(criteria);
  }

  @SelectProvider(type = DaoProvider.class, method = "getBy")
  E getBy(Criteria<E> criteria);

  /**
   * 获取所有实体列表。
   * 
   * @return 返回所有实体列表。
   */
  default List<E> getAll() {
    return getAll(null);
  }

  /**
   * 获取所有实体列表。
   * 
   * @param orderBy 排序条件
   * @return 返回所有实体列表。
   */
  @SelectProvider(type = DaoProvider.class, method = "getAll")
  List<E> getAll(String orderBy);

  /**
   * 查找满足条件的实体列表。
   * 
   * @param name 属性名
   * @param value 属性值
   * @return 返回满足条件的实体列表。
   */
  default List<E> findBy(String name, Object value) {
    return findBy(name, value, null);
  }

  /**
   * 查找满足条件的实体列表。
   * 
   * @param name 属性名
   * @param value 属性值
   * @param orderBy 排序条件
   * @return 返回满足条件的实体列表。
   */
  default List<E> findBy(String name, Object value, String orderBy) {
    Criteria<E> criteria = createCriteria();
    criteria.eq(name, value);
    criteria.orderBy(orderBy);
    return findBy(criteria);
  }

  /**
   * 查找满足条件的实体列表。
   * 
   * @param criteria 查询条件
   * @return 返回满足条件的实体列表。
   */
  @SelectProvider(type = DaoProvider.class, method = "findBy")
  List<E> findBy(Criteria<?> criteria);

  /**
   * 分页查找实体。
   * 
   * @param query 分页条件
   * @return 返回实体分页对象。
   */
  default Page<E> findPage(PageQuery query) {
    return findPage(query, createCriteria());
  }

  /**
   * 分页查找实体。
   * 
   * @param query 分页条件
   * @param criteria 查询条件
   * @return 返回实体分页对象。
   */
  default Page<E> findPage(PageQuery query, Criteria<E> criteria) {
    PageHelper.startPage(query.getPageNum(), query.getPageSize());
    PageInfo<E> pageList = findPageInfo(query, criteria);
    return PageUtils.toPage(pageList);
  }

  /**
   * 分页查找实体，返回PageHelper插件查询的分页列表对象，通常情况下不直接调用该方法。
   * 
   * @param query 分页条件
   * @param criteria 查询条件
   * @return 返回PageHelper插件查询的PageInfo对象。
   */
  @SelectProvider(type = DaoProvider.class, method = "findBy")
  PageInfo<E> findPageInfo(PageQuery query, @Param("criteria") Criteria<E> criteria);

  /**
   * 新增实体。
   * 
   * @param entity 实体
   */
  @InsertProvider(type = DaoProvider.class, method = "add")
  void add(E entity);

  /**
   * 更新实体。
   * 
   * @param entity 实体模型
   * @param fieldNames 待更新属性
   */
  @UpdateProvider(type = DaoProvider.class, method = "update")
  void update(@Param("entity") E entity, @Param("fieldNames") String... fieldNames);

  /**
   * 删除实体。
   * 
   * @param id 实体ID
   */
  default void delete(Serializable id) {
    Criteria<E> criteria = createCriteria();
    criteria.eq(criteria.getEntityMeta().getIdMeta().getProperty(), id);
    deleteBy(criteria);
  }

  /**
   * 根据条件删除实体。
   * 
   * @param name 属性名
   * @param value 属性值
   */
  default void deleteBy(String name, Object value) {
    Criteria<E> criteria = createCriteria();
    criteria.eq(name, value);
    deleteBy(criteria);
  }

  /**
   * 根据条件删除实体。
   * 
   * @param criteria 查询条件
   */
  @DeleteProvider(type = DaoProvider.class, method = "deleteBy")
  void deleteBy(Criteria<E> criteria);

  /**
   * 统计实体总数。
   * 
   * @return 返回实体总数。
   */
  default Integer count() {
    return count(createCriteria());
  }

  /**
   * 统计符合条件的实体总数。
   * 
   * @param criteria 查询条件
   * @return 返回符合条件的实体总数。
   */
  @SelectProvider(type = DaoProvider.class, method = "count")
  Integer count(Criteria<E> criteria);

  /**
   * 判断是否存在实体。
   * 
   * @param entity 实体模型
   * @param fieldNames 待比较属性名
   * @return 如果存在返回true，否则返回false。
   */
  @SelectProvider(type = DaoProvider.class, method = "isExist")
  Boolean isExist(@Param("entity") E entity, @Param("fieldNames") String... fieldNames);
}
