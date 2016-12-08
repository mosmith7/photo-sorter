package com.smithies.cassandra.common;

public class DefaultCassandraSettings implements CassandraSettings {

	private static final Integer DEFAULT_CASSANDRA_PORT = 8888;
	 /**
	   * IP address of the Cassandra cluster associated with this service.
	   * 
	   * Set through the hectorProperties PropertiesFactoryBean.
	   */
	  private String cassandraIPAddress;

	  /**
	   * Port of the Cassandra cluster associated with this service.
	   */
	  private Integer cassandraPort;

	  /**
	   * Name of the Cassandra cluster associated with this service.
	   * 
	   * Set through the hectorProperties PropertiesFactoryBean
	   */
	  private String cassandraClusterName;

	  /**
	   * Name of the Cassandra keyspace associated with this service.
	   * 
	   * Set through the hectorProperties PropertiesFactoryBean
	   */
	  private String keyspaceName;

	  /**
	   * The default replication factor to be set for new keyspaces as they are created.
	   */
	  private Integer defaultReplicationFactor;

	  @Override
	  public String getCassandraIPAddress() {
	    return cassandraIPAddress;
	  }

	  @Override
	  public Integer getCassandraPort() {
	    return cassandraPort != null ? cassandraPort : DEFAULT_CASSANDRA_PORT;
	  }

	  @Override
	  public String getCassandraClusterName() {
	    return cassandraClusterName;
	  }

	  @Override
	  public String getKeyspaceName() {
	    return keyspaceName;
	  }

	  @Override
	  public Integer getDefaultReplicationFactor() {
	    return defaultReplicationFactor;
	  }

	  public void setCassandraIPAddress(String cassandraIPAddress) {
	    this.cassandraIPAddress = cassandraIPAddress;
	  }

	  public void setCassandraPort(Integer cassandraPort) {
	    this.cassandraPort = cassandraPort;
	  }

	  public void setCassandraClusterName(String cassandraClusterName) {
	    this.cassandraClusterName = cassandraClusterName;
	  }

	  public void setKeyspaceName(String keyspaceName) {
	    this.keyspaceName = keyspaceName;
	  }

	  public void setDefaultReplicationFactor(Integer defaultReplicationFactor) {
	    this.defaultReplicationFactor = defaultReplicationFactor;
	  }

}
