package com.smithies.cassandra.common;

public interface CassandraSettings {

	String getCassandraIPAddress();

	Integer getCassandraPort();

	String getCassandraClusterName();

	String getKeyspaceName();

	Integer getDefaultReplicationFactor();

}
