package com.smithies.es09.common;

public class ElasticSearchType {

  private String name;
  private ElasticSearchIndex index;

  public ElasticSearchType(final String type, final ElasticSearchIndex index) {
    this.name = type;
    this.index = index;
  }

  public String getName() {
    return name;
  }

  public ElasticSearchIndex getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return "ElasticSearchType [name=" + name + ", index=" + index + "]";
  }

}
