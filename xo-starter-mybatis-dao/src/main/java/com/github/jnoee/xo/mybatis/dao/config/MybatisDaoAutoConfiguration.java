package com.github.jnoee.xo.mybatis.dao.config;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.jnoee.xo.mybatis.dao.ResultMapInterceptor;

@Configuration
public class MybatisDaoAutoConfiguration {
  @Bean
  ConfigurationCustomizer daoMybatisConfigurationCustomizer() {
    return configuration -> configuration.addInterceptor(new ResultMapInterceptor());
  }
}
