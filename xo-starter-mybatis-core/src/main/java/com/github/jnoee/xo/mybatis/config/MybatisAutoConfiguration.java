package com.github.jnoee.xo.mybatis.config;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.jnoee.xo.mybatis.autofill.AutoFillInterceptor;
import com.github.jnoee.xo.mybatis.autofill.AutoFillManager;
import com.github.jnoee.xo.mybatis.autofill.TimeAutoFillAware;
import com.github.jnoee.xo.mybatis.id.IdInterceptor;
import com.github.jnoee.xo.mybatis.usertype.ArrayTypeHandler;
import com.github.jnoee.xo.mybatis.usertype.ListTypeHandler;

@Configuration
public class MybatisAutoConfiguration {
  @Bean
  ConfigurationCustomizer defaultMybatisConfigurationCustomizer() {
    return configuration -> {
      configuration.setMapUnderscoreToCamelCase(true);
      configuration.setCacheEnabled(true);
      configuration.setLazyLoadingEnabled(true);
      configuration.setAggressiveLazyLoading(false);
      configuration.getTypeHandlerRegistry().register(ArrayTypeHandler.class);
      configuration.getTypeHandlerRegistry().register(ListTypeHandler.class);
      configuration.addInterceptor(new IdInterceptor());
      configuration.addInterceptor(new AutoFillInterceptor());
    };
  }

  @Bean
  TimeAutoFillAware timeAutoFillAware() {
    return new TimeAutoFillAware();
  }

  @Bean
  AutoFillManager autoFillManager() {
    return new AutoFillManager();
  }
}
