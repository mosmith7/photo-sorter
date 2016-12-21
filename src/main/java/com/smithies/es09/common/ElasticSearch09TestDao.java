package com.smithies.es09.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smithies.jackson.common.FasterObjectMapper;

@Component
class ElasticSearch09TestDao {

  private static final Logger LOG = LoggerFactory.getLogger(ElasticSearch09TestDao.class);

  // Fields
  private static final String ID = "id";
  private static final String SEARCHABLE_ALPHABET = "searchableAlphabet";
  private static final String UPDATED = "updated";
  private static final String CONTENT = "content";

  public static final String INDEX = "account-notes";
  private static final ElasticSearchIndex ES_INDEX = new ElasticSearchIndex(INDEX);

  private static final String TYPE = "accountnote";
  private static final ElasticSearchType ES_TYPE = new ElasticSearchType(TYPE, ES_INDEX);

  @Autowired
  private Client client;

  @Autowired
  private ElasticSearchAdmin elasticSearchAdmin;

  @Autowired
  private FasterObjectMapper mapper;

  @Autowired
  public void initialiseIndex() {
    final ElasticSearchIndexHelper helper = new ElasticSearchIndexHelper();
    Map<String, Object> base = helper.withBase();

    // Searchable fields
    helper.addNotAnalysed(ID);
    helper.addNotAnalysed(SEARCHABLE_ALPHABET);
    helper.addNotAnalysed(UPDATED);

    // Non searchable fields
    helper.addIndexEqualsNo(CONTENT);

    this.elasticSearchAdmin.createTypeIfDoesntExist(ES_TYPE, base);
  }

  public void save(final TestModel model) {
    LOG.trace("++ save({})", model);
    try {
      elasticSearchAdmin.put(ES_TYPE, model.getId(), model);
      LOG.info("test model {} saved", model);
    } catch (Exception e) {
      LOG.error("Failed to write to elasticsearch" + e);
    }
  }

  public List<TestModel> search(TestSearchRequest request) {
    LOG.trace("++ search({})", request);
    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX).setTypes(TYPE);

    // Add filtering by accountId and adminUserId to query
    boolean filerSet = false;
    BoolFilterBuilder boolFilterBuilder = FilterBuilders.boolFilter();
    if (request.getId() != null) {
      BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();
      boolFilter.must(FilterBuilders.termFilter(ID, request.getId()));
      boolFilterBuilder.must(boolFilter);
      filerSet = true;
    }
    if (request.getSearchableAlphabet() != null) {
      BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();
      boolFilter
          .must(FilterBuilders.termFilter(SEARCHABLE_ALPHABET, request.getSearchableAlphabet()));
      boolFilterBuilder.must(boolFilter);
      filerSet = true;
    }

    QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
    if (filerSet) {
      FilteredQueryBuilder filteredQueryBuilder =
          QueryBuilders.filteredQuery(queryBuilder, boolFilterBuilder);
      searchRequestBuilder.setQuery(filteredQueryBuilder);
    } else {
      searchRequestBuilder.setQuery(queryBuilder);
    }

    // Add ordering to query
    if (StringUtils.isNotBlank(request.getOrderBy())) {
      SortOrder sortOrder = request.getAsc() ? SortOrder.ASC : SortOrder.DESC;
      FieldSortBuilder sortBuilder = SortBuilders.fieldSort(request.getOrderBy()).order(sortOrder);
      searchRequestBuilder.addSort(sortBuilder);
    }

    // Add results from and number of results to query
    searchRequestBuilder.setFrom(request.getFirstResult());
    searchRequestBuilder.setSize(request.getPageSize());

    // Execute query and return results
    SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();;
    List<TestModel> models = new ArrayList<>();
    for (SearchHit searchHit : searchResponse.getHits()) {
      models.add(mapper.unmarshall(searchHit.getSourceAsString(), TestModel.class));
    }
    return models;
  }

  public void delete(UUID id) {
    LOG.trace("++ delete({})", id);
    try {
      elasticSearchAdmin.delete(ES_TYPE, id.toString());
      LOG.info("note {} deleted", id);
    } catch (Exception e) {
      LOG.error("Failed to delete note from elasticsearch" + e);
    }
  }

  public void refreshIndex() {
    elasticSearchAdmin.refreshIndex(ES_INDEX);
  }
}
