package com.smithies.es09.common;

public class ElasticSearchIndex {

  private final String name;

  public ElasticSearchIndex(String index) {
    this.name = index;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "ElasticSearchIndex [name=" + name + "]";
  }

}
