package com.github.jnoee.xo.mybatis.autofill;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.jnoee.xo.exception.SysException;
import com.github.jnoee.xo.utils.SpringUtils;

/**
 * 自动填充管理组件。
 */
public class AutoFillManager implements ApplicationContextAware {
  private Map<String, AutoFillAware> autoFillAwares = new HashMap<>();

  /**
   * 获取指定名称的自动填充值。
   * 
   * @param name 自动填充变量名
   * @return 返回自动填充值。
   */
  public Object getVar(String name) {
    AutoFillAware aware = autoFillAwares.get(name);
    if (aware == null) {
      throw new SysException("没有找到名称为[" + name + "]的自动填充组件。");
    }
    return aware.getValue();
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    Map<String, AutoFillAware> autoFillAwareMap =
        applicationContext.getBeansOfType(AutoFillAware.class);
    autoFillAwareMap.values().forEach(aware -> autoFillAwares.put(aware.getName(), aware));
  }

  public static AutoFillManager getInstance() {
    return SpringUtils.getBean(AutoFillManager.class);
  }
}
