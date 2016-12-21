package com.smithies.es09.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.collect.UnmodifiableIterator;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.smithies.jackson.common.FasterObjectMapper;

@Service
public class ElasticSearchAdmin {

  Logger LOG = LoggerFactory.getLogger(this.getClass());

  public static void enableTTL(final Map<String, Object> properties) {
    final Map<String, Object> ttl = new HashMap<String, Object>();
    ttl.put("enabled", true);
    properties.put("_ttl", ttl);
  }

  public static Map<String, Object> dontAnalyse() {
    final Map<String, Object> settings = new HashMap<String, Object>();
    settings.put("type", "string");
    settings.put("index", "not_analyzed");
    return settings;
  }

  public static Map<String, Object> dontAnalyseDynamicStringsMatchingPath(
      final String pathPattern) {
    final Map<String, Object> map = new HashMap<String, Object>();
    map.put("path_match", pathPattern);
    map.put("match_mapping_type", "string");
    map.put("mapping", dontAnalyse());
    return map;
  }

  public static Map<String, Object> dontAnalyseDynamicStringsMatching(final String fieldPattern) {
    final Map<String, Object> map = new HashMap<String, Object>();
    map.put("match", fieldPattern);
    map.put("match_mapping_type", "string");
    map.put("mapping", dontAnalyse());
    return map;
  }

  private static final int MAX_SIZE = 100000;

  @Autowired(required = false)
  private Client client;
  @Autowired(required = false)
  private FasterObjectMapper mapper;

  public void createTypeIfDoesntExist(final ElasticSearchTypeAliased type,
      final Map<String, Object> base) {
    // Get the current index and aliases
    final ElasticSearchIndex currentIndex = type.getCurrentIndex();
    final ElasticSearchIndex readAlias = type.getReadAlias();
    final ElasticSearchIndex writeAlias = type.getWriteAlias();
    final String currentIndexName = currentIndex.getName();
    final String readAliasname = readAlias.getName();
    final String writeAliasName = writeAlias.getName();

    // Create the type if it index exist
    createTypeIfDoesntExist(base, type.getName(), currentIndex);

    // Create a new alias modification request
    final IndicesAliasesRequest aliasModificationRequest = new IndicesAliasesRequest();

    // Add the read alias so that we are reading data from the newly created
    // index
    aliasModificationRequest.addAlias(currentIndexName, readAliasname);

    // The write alias should only point at the most recent index. So remove
    // all the current mappings for the write index.
    final Multimap<String, String> listAliasesAndTheirIndexes = listAliasesAndTheirIndexes();
    for (final String indexName : listAliasesAndTheirIndexes.get(writeAliasName)) {
      aliasModificationRequest.removeAlias(indexName, writeAliasName);
    }

    // Add the write alias so that we are writing to the newly created index
    aliasModificationRequest.addAlias(currentIndexName, writeAliasName);

    // Apply the alias modification request
    applyAliasesModificationRequest(aliasModificationRequest);
  }

  public void copyAlias(final String aliasName, final String aliasNameExample) {
    // List all the alias details
    final Multimap<String, String> listAliasesAndTheirIndexes = listAliasesAndTheirIndexes();

    if (listAliasesAndTheirIndexes.containsKey(aliasNameExample)) {

      // Create a new alias modification request
      final IndicesAliasesRequest aliasModificationRequest = new IndicesAliasesRequest();

      // Get the current details for the alias and remove all of them
      for (final String indexName : listAliasesAndTheirIndexes.get(aliasName)) {
        aliasModificationRequest.removeAlias(indexName, aliasName);
      }

      // Get the current details for the alias example and copy all of
      // them
      for (final String indexName : listAliasesAndTheirIndexes.get(aliasNameExample)) {
        aliasModificationRequest.addAlias(indexName, aliasName);
      }

      // Apply the alias modification request
      applyAliasesModificationRequest(aliasModificationRequest);

    } else {

      LOG.warn("Not doing anything because example index does not exist: {}", aliasNameExample);

    }
  }

  private void applyAliasesModificationRequest(final IndicesAliasesRequest request) {
    try {
      this.client.admin().indices().aliases(request).actionGet();
    } catch (final Exception e) {
      LOG.warn("Failed to modify aliases: {}", request);
    }
  }

  public Multimap<String, String> listIndexesAndTheirAliases() {
    final ArrayListMultimap<String, String> map = ArrayListMultimap.create();
    final ImmutableOpenMap<String, ImmutableOpenMap<String, AliasMetaData>> aliases =
        this.client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData()
            .getAliases();

    final UnmodifiableIterator<ImmutableOpenMap<String, AliasMetaData>> aliasIt =
        aliases.valuesIt();
    for (ImmutableOpenMap<String, AliasMetaData> indexes; aliasIt.hasNext();) {
      indexes = aliasIt.next();
      final UnmodifiableIterator<String> indexIt2 = indexes.keysIt();
      for (String index; indexIt2.hasNext();) {
        index = indexIt2.next();
        final AliasMetaData aliasMetaData = indexes.get(index);
        map.put(index, aliasMetaData.getAlias());
      }
    }
    return map;
  }

  /**
   * @return a multimap of the names of all the registered and aliases their indexes
   */
  public Multimap<String, String> listAliasesAndTheirIndexes() {
    final ArrayListMultimap<String, String> map = ArrayListMultimap.create();
    final ImmutableOpenMap<String, ImmutableOpenMap<String, AliasMetaData>> aliases =
        this.client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData()
            .getAliases();

    final UnmodifiableIterator<ImmutableOpenMap<String, AliasMetaData>> aliasIt =
        aliases.valuesIt();
    for (ImmutableOpenMap<String, AliasMetaData> indexes; aliasIt.hasNext();) {
      indexes = aliasIt.next();
      final UnmodifiableIterator<String> indexIt2 = indexes.keysIt();
      for (String index; indexIt2.hasNext();) {
        index = indexIt2.next();
        final AliasMetaData aliasMetaData = indexes.get(index);
        map.put(aliasMetaData.getAlias(), index);
      }
    }
    return map;
  }

  public void createTypeIfDoesntExist(final ElasticSearchType type,
      final Map<String, Object> base) {
    final String name = type.getName();
    final ElasticSearchIndex index = type.getIndex();
    createTypeIfDoesntExist(base, name, index);
  }

  public void updateMappings(final ElasticSearchType type, final Map<String, Object> base) {
    LOG.info("Update mappings for Type {} with base {}", type, base);

    // Check if the index already exists
    final PutMappingResponse response =
        this.client.admin().indices().putMapping(new PutMappingRequest(type.getIndex().getName())
            .source(base).ignoreConflicts(false).type(type.getName())).actionGet();
    ElasticSearchUtils.log("Updated Mappings", response);
  }

  private void createTypeIfDoesntExist(final Map<String, Object> base, final String typename,
      final ElasticSearchIndex index) {
    LOG.info("Initialising Type {} {} with {}", typename, index, base);

    // Check if the index already exists
    final boolean indicesExists = this.client.admin().indices().prepareExists(index.getName())
        .execute().actionGet().isExists();

    if (!indicesExists) {

      // Prepare to create the new mapping
      final CreateIndexResponse response = this.client.admin().indices()
          .prepareCreate(index.getName()).addMapping(typename, base).execute().actionGet();

      ElasticSearchUtils.log("Create Index", response);
      this.refreshIndex(index);

    } else {
      // Check if the type already exists
      final boolean typeExists = this.client.admin().indices().prepareTypesExists(index.getName())
          .setTypes(typename).execute().actionGet().isExists();
      if (!typeExists) {
        // Prepare to create the new mapping
        final PutMappingResponse response =
            this.client.admin().indices().preparePutMapping(index.getName()).setType(typename)
                .setSource(base).execute().actionGet();

        ElasticSearchUtils.log("Create Index", response);
        this.refreshIndex(index);
      } else {
        LOG.info("Type already exists for {}, so not creating anything", index);
      }

    }

    logType(index);
  }

  private void logType(final ElasticSearchIndex index) {
    try {
      final ClusterState clusterState = this.client.admin().cluster().prepareState()
          .setFilterIndices(index.getName()).execute().actionGet().getState();
      final IndexMetaData metaData = clusterState.getMetaData().index(index.getName());
      final MappingMetaData mappingMetaData = metaData.mappings().get("details");

      String source;
      if (mappingMetaData != null) {
        source = mappingMetaData.source().toString();
      } else {
        source = "";
      }
      LOG.info("Search Type {} has Settings {} and has Mappings {} ", index,
          metaData.getSettings().toDelimitedString(','), source);
    } catch (final Exception e) {
      LOG.warn("Failed to print out details for the index {} {}", index.getName(), e.getMessage());
    }
  }

  public void refreshIndex(final ElasticSearchIndex index) {
    refreshIndex(index.getName());
  }

  public void refreshIndex(final String name) {
    final RefreshResponse response =
        this.client.admin().indices().refresh(new RefreshRequest(name)).actionGet();
    LOG.debug("Refreshing Index: {}", name);
    ElasticSearchUtils.debug("Refesh", response);
  }

  public void waitForHappy(final ElasticSearchIndex index) {
    final ClusterHealthResponse response = this.client.admin().cluster()
        .prepareHealth(index.getName()).setWaitForYellowStatus().execute().actionGet();
    LOG.debug("Wait for happy: {}", response.getStatus());
  }

  public void deleteIndex(final ElasticSearchIndex index) {
    final DeleteIndexResponse response =
        this.client.admin().indices().prepareDelete(index.getName()).execute().actionGet();
    LOG.info("Deleting Index: {}", index);
    ElasticSearchUtils.log("Delete", response);
  }

  public DeleteResponse delete(final ElasticSearchType type, final String id) {
    return this.client.prepareDelete(type.getIndex().getName(), type.getName(), id).execute()
        .actionGet();
  }

  public void delete(final ElasticSearchTypeAliased type, final UUID uuid) {
    LOG.info("Deleting {}: {}", type.getName(), uuid);
    checkNotNull(uuid, "Attempt to delete a null UUID");

    final DeleteResponse response =
        this.client.prepareDelete(type.getWriteAlias().getName(), type.getName(), uuid.toString())
            .execute().actionGet();
    if (response.isNotFound()) {
      throw new RuntimeException("No index found for " + type + " and " + uuid);
    }

  }

  public void deleteAllDocumentsIn(final ElasticSearchType type) {
    deleteAllDocumentsIn(type.getName());
  }

  public void deleteAllDocumentsIn(final String name) {
    client
        .deleteByQuery(new DeleteByQueryRequest().types(name).query(QueryBuilders.matchAllQuery()))
        .actionGet();
  }

  public <T> T get(final ElasticSearchTypeAliased type, final UUID uuid, final Class<T> class1) {
    LOG.debug("Getting search index for {}: {}", type.getName(), uuid);
    checkNotNull(uuid, "Attempt to get a null UUID");
    final GetResponse response = get(type, uuid.toString());
    if (!response.isExists()) {
      throw new RuntimeException("Not Found: " + uuid);
    }

    final String sourceAsString = response.getSourceAsString();
    return this.mapper.unmarshall(sourceAsString, class1);
  }

  public <T> T get(final ElasticSearchType type, final UUID uuid, final Class<T> class1) {
    final GetResponse response = get(type, uuid.toString());
    if (!response.isExists()) {
      throw new RuntimeException("Not Found: " + uuid);
    }
    return this.mapper.unmarshall(response.getSourceAsString(), class1);
  }

  public <T> T get(final ElasticSearchType type, final String id, final Class<T> class1) {
    final GetResponse response = get(type, id);
    if (!response.isExists()) {
      throw new RuntimeException("Not Found: " + id);
    }
    return this.mapper.unmarshall(response.getSourceAsString(), class1);
  }

  public GetResponse get(final ElasticSearchType type, final String id) {
    return this.client.prepareGet(type.getIndex().getName(), type.getName(), id).execute()
        .actionGet();
  }

  private GetResponse get(final ElasticSearchTypeAliased type, final String id) {
    return this.client.prepareGet(type.getReadAlias().getName(), type.getName(), id).execute()
        .actionGet();
  }

  public SearchRequestBuilder prepareSearch(final ElasticSearchType type) {
    return this.client.prepareSearch(type.getIndex().getName()).setTypes(type.getName());
  }

  public List<String> getAllIds(ElasticSearchType type) {
    SearchHits hits = matchAllQuery(type).setNoFields().execute().actionGet().getHits();
    ArrayList<String> ids = new ArrayList<String>();
    for (SearchHit searchHit : hits) {
      ids.add(searchHit.getId());
    }
    return ids;
  }

  public <R> List<R> getAll(final ElasticSearchType type, final Class<R> clazz) {
    return getAll(type, (hit) -> mapper.unmarshall(hit.getSourceAsString(), clazz));
  }

  public <R> List<R> getAll(final ElasticSearchType type, final Function<SearchHit, R> function) {
    final ArrayList<R> list = new ArrayList<R>();
    for (final SearchHit hit : matchAllQuery(type).execute().actionGet().getHits()) {
      final R apply = function.apply(hit);
      if (apply != null) {
        list.add(apply);
      }
    }
    return list;
  }

  private SearchRequestBuilder matchAllQuery(final ElasticSearchType type) {
    return client.prepareSearch(type.getIndex().getName()).setTypes(type.getName())
        .setQuery(QueryBuilders.matchAllQuery()).setSize(MAX_SIZE);
  }

  // @Deprecated
  // Use ElasticSearchTypeAliased version
  public void put(final ElasticSearchType type, final Object id, final Object data) {
    final String name = type.getName();
    final ElasticSearchIndex index = type.getIndex();
    put(name, index, id, data);
  }

  public void put(final ElasticSearchTypeAliased type, final UUID uuid, final Object data) {
    LOG.trace("Saving record {} {}", type, uuid);
    checkNotNull(data, "Attempt to index null data");
    checkNotNull(uuid, "Attempt to index a with a null UUID");
    final String name = type.getName();
    final ElasticSearchIndex index = type.getWriteAlias();
    put(name, index, uuid, data);
  }

  public void put(final ElasticSearchTypeAliased type, final UUID uuid, final Object data,
      final Long ttlMillis) {
    LOG.trace("Saving record {} {}", type, uuid);
    checkNotNull(data, "Attempt to index null data");
    checkNotNull(uuid, "Attempt to index a with a null UUID");
    final String name = type.getName();
    final ElasticSearchIndex index = type.getWriteAlias();
    if (ttlMillis == null) {
      put(name, index, uuid, data);
    } else {
      put(name, index, uuid, data, ttlMillis);
    }
  }

  private void put(final String name, final ElasticSearchIndex index, final Object id,
      final Object data) {
    checkNotNull(id, "Attempt to index a null id");
    checkNotNull(data, "Attempt to index null data");
    try {
      final String json = this.mapper.marshall(data);

      final IndexRequest indexRequest =
          this.client.prepareIndex(index.getName(), name, id.toString()).setSource(json).request();
      this.client.index(indexRequest).actionGet();
    } catch (final Exception e) {
      LOG.error("Error thrown during indexing [{}]", e.getMessage(), e);
      throw new RuntimeException("Error thrown during indexing: " + e.getMessage(), e);
    }
  }

  private void put(final String name, final ElasticSearchIndex index, final UUID uuid,
      final Object data, final long ttlMillis) {
    checkNotNull(uuid, "Attempt to index a null id");
    checkNotNull(data, "Attempt to index null data");
    try {
      final String json = this.mapper.marshall(data);

      final IndexRequest indexRequest =
          this.client.prepareIndex(index.getName(), name, uuid.toString()).setSource(json)
              .setTTL(ttlMillis).request();
      this.client.index(indexRequest).actionGet();
    } catch (final Exception e) {
      LOG.error("Error thrown during indexing [{}]", e.getMessage(), e);
      throw new RuntimeException("Error thrown during indexing: " + e.getMessage(), e);
    }
  }

  // @Deprecated
  // // Use ElasticSearchTypeAliased version
  // public <T> PagedResults<T> query(final ElasticSearchType type, final CompoundSearchFilter
  // filter,
  // final SortBuilder[] sortBuilders, final int pageSize, final int from, final Class<T> clazz) {
  // final String typeName = type.getName();
  // final ElasticSearchIndex index = type.getIndex();
  // return query(
  // ElasticsearchQuery.apply(typeName, index, filter, sortBuilders, pageSize, from, clazz));
  // }
  //
  // public <T> PagedResults<T> query(final ElasticSearchTypeAliased type,
  // final CompoundSearchFilter filter, final SortBuilder[] sortBuilders, final int pageSize,
  // final int from, final Class<T> clazz) {
  // final String typeName = type.getName();
  // final ElasticSearchIndex index = type.getReadAlias();
  // return query(
  // ElasticsearchQuery.apply(typeName, index, filter, sortBuilders, pageSize, from, clazz));
  // }
  //
  // public <T> PagedResults<T> query(final ElasticsearchQuery<T> query) {
  // final String typeName = query.getTypeName();
  // final ElasticSearchIndex index = query.getIndex();
  // final CompoundSearchFilter filter = query.getFilter();
  // final SortBuilder[] sortBuilders = query.getSortBuilders();
  // final int pageSize = query.getPageSize();
  // final int from = query.getFrom();
  // final Class<T> clazz = query.getClazz();
  //
  // // Build the query
  // QueryBuilder builder = QueryBuilders.matchAllQuery();
  //
  // // If we have any filters then apply them to the query
  // if (filter.hasSomethingToFilter()) {
  // builder = QueryBuilders.filteredQuery(builder, filter.toFilterBuilder());
  // }
  //
  // // Build a query from the original request
  // final SearchRequestBuilder requestBuilder = this.client.prepareSearch(index.getName())
  // .setTypes(typeName).setSize(pageSize).setFrom(from);
  //
  // // Execute the query
  // requestBuilder.setQuery(builder);
  //
  // if (query.isRandomOrder()) {
  // final FunctionScoreQueryBuilder functionScoreQueryBuilder =
  // new FunctionScoreQueryBuilder(builder);
  // functionScoreQueryBuilder.add(new RandomScoreFunctionBuilder());
  // requestBuilder.setQuery(functionScoreQueryBuilder);
  // }
  //
  // if (sortBuilders != null) {
  // for (final SortBuilder sortBuilder : sortBuilders) {
  // requestBuilder.addSort(sortBuilder);
  // }
  // }
  // // TODO need a conventient way of specifying the sort order
  // // requestBuilder.addSort(new
  // // FieldSortBuilder("timestamp").order(SortOrder.DESC)
  // // .missing("_last"));
  //
  // LOG.debug("Search Request {}", requestBuilder);
  //
  // SearchResponse searchResponse;
  // try {
  // searchResponse = requestBuilder.execute().get();
  // } catch (final Exception e) {
  // throw new RuntimeException(e);
  // }
  //
  // // Unmarshall the results into a list
  // final List<T> list = new ArrayList<T>();
  // for (final SearchHit searchHit : searchResponse.getHits()) {
  // final T item = this.mapper.unmarshall(searchHit.getSourceAsString(), clazz);
  // list.add(item);
  // }
  //
  // final SearchHits searchHits = searchResponse.getHits();
  // final long hits = searchHits.getTotalHits();
  //
  // final PagedResults<T> dataTableWrapper = new PagedResults<T>();
  // dataTableWrapper.setRecordsTotal(hits);
  //
  // dataTableWrapper.setRecordsFiltered(hits);
  // dataTableWrapper.setData(list);
  // dataTableWrapper.setRecordsTotal(hits);
  //
  // return dataTableWrapper;
  // }

  public void setTtlSweepTime(final String ttlInterval) {
    this.client.admin().cluster()
        .updateSettings(new ClusterUpdateSettingsRequest().transientSettings(
            ImmutableSettings.builder().put("indices.ttl.interval", ttlInterval).build()))
        .actionGet();
  }

}
