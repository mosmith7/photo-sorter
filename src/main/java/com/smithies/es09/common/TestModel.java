package com.smithies.es09.common;

import java.util.UUID;

public class TestModel {

  private UUID id;
  private String content;
  private String searchableAlphabet;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getSearchableAlphabet() {
    return searchableAlphabet;
  }

  public void setSearchableAlphabet(String searchableAlphabet) {
    this.searchableAlphabet = searchableAlphabet;
  }

}
