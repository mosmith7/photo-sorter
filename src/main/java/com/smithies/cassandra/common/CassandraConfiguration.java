package com.smithies.cassandra.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ComponentScan
public class CassandraConfiguration {

  public static final String KS_QUALIFIER_PHOTO_SORTER = "photoSorterCassandraKeyspaceSettings";

  @Bean(name = KS_QUALIFIER_PHOTO_SORTER)
  public CassandraKeyspaceSettings loadEnvironment(final Environment environment) {
    return CassandraKeyspaceSettings.from(environment, "photosorter", "photosorter");
  }

}
