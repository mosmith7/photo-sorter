package com.smithies.photosorter.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.smithies.cassandra.common.CassandraConfiguration;
import com.smithies.jackson.common.JacksonConfiguration;
import com.smithies.photosorter.component.filemanager.FileManagerConfig;
import com.smithies.photosorter.component.tags.TagsConfiguration;

@Configuration
@Import({CassandraConfiguration.class, FileManagerConfig.class, TagsConfiguration.class, JacksonConfiguration.class})
public class ApplicationConfiguration extends WebMvcConfigurerAdapter {
	
	@Autowired
	LogInterceptor logInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(logInterceptor);
	}
}