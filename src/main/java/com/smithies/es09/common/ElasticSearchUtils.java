package com.smithies.es09.common;

import org.elasticsearch.action.support.broadcast.BroadcastOperationResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchUtils {

  private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchUtils.class);

  public static void log(final String name, final AcknowledgedResponse response) {
    final boolean acknowledged = response.isAcknowledged();
    if (acknowledged) {
      LOG.debug("{} was acknowledged with headers: {}", name, response.getHeaders());
    } else {
      LOG.warn("{} was not acknowledged with headers: {}", name, response.getHeaders());
    }
  }

  public static void info(final String name, final BroadcastOperationResponse response) {
    final int totalShards = response.getTotalShards();
    final int failedShards = response.getFailedShards();
    final int successfulShards = response.getSuccessfulShards();
    LOG.info("{} resulted in successes {}, fails {}, out of {} in total", name, successfulShards,
        failedShards, totalShards);
  }

  public static void debug(final String name, final BroadcastOperationResponse response) {
    final int totalShards = response.getTotalShards();
    final int failedShards = response.getFailedShards();
    final int successfulShards = response.getSuccessfulShards();
    LOG.debug("{} resulted in successes {}, fails {}, out of {} in total", name, successfulShards,
        failedShards, totalShards);
  }

}
