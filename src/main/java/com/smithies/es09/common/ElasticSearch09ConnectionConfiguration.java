package com.smithies.es09.common;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


@Configuration
public class ElasticSearch09ConnectionConfiguration {

  private static final String MODE_DATALESS_NODE = "node";
  private static final String MODE_PERSISTENT_LOCAL_TEST_NODE = "local";
  private static final String MODE_TCP = "http";

  private static final String MODE_DEFAULT = MODE_TCP;

  private static final Logger LOG =
      LoggerFactory.getLogger(ElasticSearch09ConnectionConfiguration.class);

  private static boolean systemInPanicState = false;

  @Bean
  public Client getClient(final Environment environment) {
    if (systemInPanicState) {
      throw new RuntimeException(
          "Elastic search in panic state due to previous failed attempt to clean the data folder");
    }
    final String env = EnvironmentProperties.getName(environment);

    LOG.info("Create Elastic search node for " + env);

    final String clusterName =
        environment.getProperty("elasticsearch.cluster.name", env + "-elasticsearch");
    LOG.info("Elastic search cluster name {}", clusterName);

    final String discoverHost =
        environment.getProperty("elasticsearch.unicast.host", "sg_elasticsearch");
    LOG.trace("Elastic search cluster discovery host {}", discoverHost);

    final String publishHost = environment.getProperty("elasticsearch.publish.host", "");
    LOG.trace("Elastic search cluster publish host {}", publishHost);

    final Builder settings = ImmutableSettings.settingsBuilder();

    final Client client;

    // "local" means run elasicsearch local data locally - only used for testing
    // "node" means connect to a remote elasticsearch as a dataless node
    // "hhtp" means connect to a remote elasticsearch using http
    final String local = environment.getProperty("elasticsearch.local", "false");
    final String defaultMode;
    if ("true".equals(local)) {
      defaultMode = MODE_PERSISTENT_LOCAL_TEST_NODE;
    } else {
      defaultMode = MODE_DEFAULT;
    }
    final String mode = environment.getProperty("elasticsearch.mode", defaultMode);

    if (MODE_PERSISTENT_LOCAL_TEST_NODE.equals(mode)) {
      File file = new File("data");
      if (file.exists() && file.isDirectory()) {
        LOG.warn("Delete: {}", file.getAbsolutePath());
        try {
          FileUtils.deleteDirectory(file);
        } catch (IOException e) {
          systemInPanicState = true;
          throw new RuntimeException("Failed to delete " + file.getAbsolutePath(), e);
        }
      }

      LOG.trace("Local config used");
      settings.put("discovery.zen.ping.multicast.enabled", false).put("cluster.name",
          "localcluster");

      settings.put("gateway.type", "local");

      // settings.put("index.number_of_shards", "1");
      //
      // settings.put("index.number_of_replicas", "0");

      // Store data in memory
      settings.put("index.store.type", "memory");

      Consumer<String> alter = (name) -> {
        settings.put("threadpool." + name + ".size", 1);
        settings.put("threadpool." + name + ".type", "fixed");
      };
      // Reduce the size of each of the elasticsearch thread pools
      // alter.accept("index");
      // alter.accept("search");
      // alter.accept("merge");
      // alter.accept("http_server_worker");
      // alter.accept("warmer");
      // alter.accept("refresh");

      // alter.accept("suggest");
      // alter.accept("get");
      // alter.accept("bulk");
      // alter.accept("percolate");

      settings.put("http.enabled", "false");

      // Set the number of http workers to 1
      settings.put("http.netty.worker_count", 1);

      final Node node = nodeBuilder().settings(settings).local(true).client(false).build();

      LOG.info("Starting the Elastic Search node..");
      node.start();
      LOG.trace("Elastic Search node started");
      client = node.client();

    } else if (MODE_DATALESS_NODE.equals(mode)) {
      LOG.trace("External config used");
      settings.put("discovery.zen.ping.multicast.enabled", false)
          .put("discovery.zen.ping.unicast.hosts", discoverHost).put("cluster.name", clusterName)
          .put("client.transport.sniff", false);

      // If the publish host has been set then use it in the settings for
      // this node
      if (!"".equals(publishHost)) {
        settings.put("network.publish_host", publishHost);
      }
      final Node node = nodeBuilder().settings(settings).local(false).client(true).build();

      LOG.trace("Starting the Elastic Search node..");
      node.start();
      LOG.trace("Elastic Search node started");
      client = node.client();
    } else if (MODE_TCP.equals(mode)) {
      int port = 9300;

      LOG.info("Elasticsearch is using {}:{}", discoverHost, port);

      settings.put("discovery.zen.ping.multicast.enabled", false)
          .put("discovery.zen.ping.unicast.hosts", discoverHost).put("cluster.name", clusterName)
          .put("client.transport.sniff", false);

      client = new TransportClient(settings)
          .addTransportAddress(new InetSocketTransportAddress(discoverHost, port));
    } else {
      throw new RuntimeException("Unrecognised elasticsearch mode: " + mode);
    }

    return client;
  }

  // Radiantworld stuff
  // @Bean
  // public ElasticSearchAdmin newElasticSearchAdmin() {
  // return new ElasticSearchAdmin();
  // }
  //
  // @Bean
  // public ElasticSearchIndexMigrator newElasticSearchIndexMigrator() {
  // return new ElasticSearchIndexMigrator();
  // }
  //
  // @Bean
  // public ElasticsearchHealthCheckService newElasticsearchHealthCheckService() {
  // return new ElasticsearchHealthCheckService();
  // }
}
