package com.smithies.es09.common;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
@Import(ElasticSearch09ConnectionConfiguration.class)
public class ElasticSearch09Configuration {

  private static final Logger LOG = LoggerFactory.getLogger(ElasticSearch09Configuration.class);

  @Autowired(required = false)
  private List<ElasticSearch09IndexInitialiser> initialisers;

  @PostConstruct
  public void init() {
    LOG.info("Intialising elasticsearch");

    // Iterate over the list of elastic search initialisers and run them in
    // turn
    if (initialisers != null) {
      for (ElasticSearch09IndexInitialiser elasticSearchIndexInitialiser : initialisers) {
        elasticSearchIndexInitialiser.initialise();
      }
    }
  }

}
