package com.smithies.es09.common;

import java.util.UUID;

public class TestSearchRequest {

  private UUID id;
  private String searchableAlphabet;

  private String orderBy;
  private Boolean asc;

  private int firstResult = 0;
  private int pageSize = 25;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getSearchableAlphabet() {
    return searchableAlphabet;
  }

  public void setSearchableAlphabet(String searchableAlphabet) {
    this.searchableAlphabet = searchableAlphabet;
  }

  public String getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(String orderBy) {
    this.orderBy = orderBy;
  }

  public Boolean getAsc() {
    return asc;
  }

  public void setAsc(Boolean asc) {
    this.asc = asc;
  }

  public int getFirstResult() {
    return firstResult;
  }

  public void setFirstResult(int firstResult) {
    this.firstResult = firstResult;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }
}


