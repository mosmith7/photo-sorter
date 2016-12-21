package com.smithies.photosorter.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.smithies.cassandra.common.CassandraConfiguration;
import com.smithies.es09.common.ElasticSearch09Configuration;
import com.smithies.jackson.common.JacksonConfiguration;
import com.smithies.photosorter.component.filemanager.FileManagerConfig;
import com.smithies.photosorter.component.tags.TagsConfiguration;
import com.smithies.redis.common.RedisConfiguration;

@Configuration
// @EnableAutoConfiguration(exclude = {CassandraDataAutoConfiguration.class})
@Import({DispatcherServletConfiguration.class, ElasticSearch09Configuration.class,
    CassandraConfiguration.class, FileManagerConfig.class, TagsConfiguration.class,
    JacksonConfiguration.class, RedisConfiguration.class})
public class ApplicationConfiguration extends WebMvcConfigurerAdapter {

  @Autowired
  LogInterceptor logInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(logInterceptor);
  }
}
