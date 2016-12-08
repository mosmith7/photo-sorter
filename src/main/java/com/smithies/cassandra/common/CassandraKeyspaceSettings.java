package com.smithies.cassandra.common;

import org.springframework.core.env.Environment;

public class CassandraKeyspaceSettings {


		  private static final String CASSANDRA = "localhost";
		  private static final int BASE_SLEEP_TIME = 250;
		  private static final int MAX_RETRY_ATTEMPTS = 5;
		  private static final int HOSTS = 1;
		  private static final int CORES_PER_HOST = 2;
		  private static final int REPLICATION_FACTOR = 1;

		  private static final int SPARE_CONNECTIONS_PER_HOST = 3 + 2;

		  // Here host refers to each cassandra node. Hence if you have a 6 node
		  // cluster and if you set MaxConnsPerHost=5, then from a single Astyanax
		  // you will have 30 connections to the enturecluster.
		  private static final int MAX_CONNECTIONS_PER_CORE = 3;

		  private static final int MAX_CONNECTIONS_PER_HOST =
		      CORES_PER_HOST * MAX_CONNECTIONS_PER_CORE / REPLICATION_FACTOR + SPARE_CONNECTIONS_PER_HOST;

		  private static final int DEFAULT_CONNECT_TIMEOUT_MS = 2 * 1000;

		  private static final int DEFAULT_SOCKET_TIMEOUT_MS = 10 * 1000;

		  private static final int MAX_CONNECTIONS = HOSTS * MAX_CONNECTIONS_PER_HOST;

		  public static CassandraKeyspaceSettings from(final Environment environment, final String name,
		      final String keyspace, boolean withRetry) {
		    final String host = environment.getProperty("cassandra.host", CASSANDRA);
		    final Integer port = environment.getProperty("cassandra.port", Integer.class, 9160);
		    final Integer replication = environment.getProperty("cassandra.replication", Integer.class, 1);

		    final String key = "cassandra." + name;
		    final CassandraKeyspaceSettings settings = new CassandraKeyspaceSettings(withRetry);
		    settings.setIPAddress(environment.getProperty(key + ".host", host));
		    settings.setPort(environment.getProperty(key + ".port", Integer.class, port));
		    settings.setReplicationFactor(
		        environment.getProperty(key + ".replication", Integer.class, replication));
		    settings.setKeyspaceName(environment.getProperty(key + ".keyspace", keyspace));
		    return settings;
		  }

		  public static CassandraKeyspaceSettings from(final Environment environment, final String name,
		      final String keyspace) {
		    return from(environment, name, keyspace, true);
		  }

		  private String iPAddress;
		  private int port;
		  private String clusterName;
		  private String keyspaceName;
		  private int replicationFactor;
		  private boolean withRetryPolicy;

		  public CassandraKeyspaceSettings() {
		    this(true);
		  }

		  public CassandraKeyspaceSettings(boolean withRetryPolicy) {
		    this.withRetryPolicy = withRetryPolicy;
		  }

		  public String getIPAddress() {
		    return this.iPAddress;
		  }

		  public void setIPAddress(final String pAddress) {
		    this.iPAddress = pAddress;
		  }

		  public int getPort() {
		    return this.port;
		  }

		  public void setPort(final int port) {
		    this.port = port;
		  }

		  public String getClusterName() {
		    return this.clusterName;
		  }

		  public void setClusterName(final String clusterName) {
		    this.clusterName = clusterName;
		  }

		  public String getKeyspaceName() {
		    return this.keyspaceName;
		  }

		  public void setKeyspaceName(final String keyspaceName) {
		    this.keyspaceName = keyspaceName;
		  }

		  public int getDefaultReplicationFactor() {
		    return this.replicationFactor;
		  }

		  public void setReplicationFactor(final int replicationFactor) {
		    this.replicationFactor = replicationFactor;
		  }


		  @Override
		  public String toString() {
		    return "CassandraKeyspaceSettingsImpl [iPAddress=" + this.iPAddress + ", port=" + this.port
		        + ", clusterName=" + this.clusterName + ", keyspaceName=" + this.keyspaceName
		        + ", replicationFactor=" + this.replicationFactor + "]";
		  }

}
