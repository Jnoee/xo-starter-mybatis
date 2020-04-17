package com.github.jnoee.xo.mybatis.autofill;

import java.util.Date;

import com.github.jnoee.xo.utils.DateUtils;

/**
 * 当前时间自动填充组件。
 */
public class TimeAutoFillAware implements AutoFillAware {
  @Override
  public String getName() {
    return AutoFillVar.TIME;
  }

  @Override
  public String getValue() {
    return DateUtils.format(new Date(), DateUtils.SECOND);
  }
}
