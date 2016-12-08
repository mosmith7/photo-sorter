package com.smithies.cassandra.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.keyspace.CreateKeyspaceSpecification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;

@Configuration
public class CassandraCqlConfig {

//	public static final String CASSANDRA_BATCH_TEMPLATE = "CassandraBatchTemplate";

	 private static final String CASSANDRA_PHOTO_SORTER_KEYSPACE = "photosorter";

	 @Autowired
	 @Qualifier(CassandraConfiguration.KS_QUALIFIER_PHOTO_SORTER)
	 private CassandraKeyspaceSettings config;

	 @Bean
	 public CassandraClusterFactoryBean cluster() {
	   CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
	   cluster.setContactPoints(config.getIPAddress());
	   List<CreateKeyspaceSpecification> specifications = new ArrayList<>();
	   specifications.add(CreateKeyspaceSpecification.createKeyspace(CASSANDRA_PHOTO_SORTER_KEYSPACE)
	       .ifNotExists().withSimpleReplication());
	   cluster.setKeyspaceCreations(specifications);
	   return cluster;
	 }

	 /**
	  * @return Cassandra mapping context
	  */
	 @Bean
	 public CassandraMappingContext mappingContext() {
	   return new BasicCassandraMappingContext();
	 }

	 /**
	  * @return Cassandra data converter
	  */
	 @Bean
	 public CassandraConverter converter() {
	   return new MappingCassandraConverter(mappingContext());
	 }

	 @Bean
	 public CassandraSessionFactoryBean session() throws Exception {
	   CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
	   session.setCluster(cluster().getObject());
	   session.setKeyspaceName(CASSANDRA_PHOTO_SORTER_KEYSPACE);
	   session.setConverter(converter());
	   // session.setSchemaAction(SchemaAction.CREATE_IF_NOT_EXISTS);
	   return session;
	 }

//	 @Bean(name = CASSANDRA_BATCH_TEMPLATE)
//	 public CassandraOperations cassandraTemplate() throws Exception {
//	   return new CassandraTemplate(session().getObject());
//	 }
}
