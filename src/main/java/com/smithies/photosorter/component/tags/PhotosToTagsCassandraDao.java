package com.smithies.photosorter.component.tags;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

@Component
class PhotosToTagsCassandraDao {

  Logger log = LoggerFactory.getLogger(this.getClass());

  // Keyspace and table
  private static final String KS = "photosorter";
  private static final String PHOTOS_TO_TAGS_TABLE = "photos_to_tags";
  private static final String PHOTO_TO_TAGS = KS + "." + PHOTOS_TO_TAGS_TABLE;

  // Schema
  private static final String PHOTO_ID = "photoId";
  private static final String TAG = "tag";
  private static final String PARTITION_KEY = PHOTO_ID;

  // SQL statements
  private static final String CQL_CREATE =
      String.format("CREATE TABLE IF NOT EXISTS %s (%s uuid, %s text, PRIMARY KEY(%s))",
          PHOTO_TO_TAGS, PHOTO_ID, TAG, PARTITION_KEY);
  private static final String INSERT_CQL =
      String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)", PHOTO_TO_TAGS, PHOTO_ID, TAG);

  private CassandraOperations cassandra;

  @Autowired
  public void setCassandra(CassandraOperations cassandra) {
    this.cassandra = cassandra;
    cassandra.execute(CQL_CREATE);
  }

  // @Autowired
  // public void setCassandra() {
  // Cluster cluster = Cluster.builder().addContactPoint("localhost").withPort(9042).build();
  // Session session = cluster.connect();
  // cassandra = new CassandraTemplate(session);
  // cassandra.execute(CQL_CREATE);
  // }

  public PhotoTagsModel addPhotoTag(AddOrRemovePhotoTagRequest request) {
    cassandra.execute(new SimpleStatement(INSERT_CQL, request.getPhotoId(), request.getTag()));
    return get(request.getPhotoId());
  }

  public PhotoTagsModel removePhotoTag(AddOrRemovePhotoTagRequest request) {
    Delete delete = QueryBuilder.delete().from(KS, PHOTOS_TO_TAGS_TABLE);
    delete.where(eq(PHOTO_ID, request.getPhotoId())).and(eq(TAG, request.getTag()));
    cassandra.execute(delete);
    return get(request.getPhotoId());
  }

  public PhotoTagsModel get(UUID id) {
    Select select = QueryBuilder.select().all().from(PHOTOS_TO_TAGS_TABLE);
    select.where(eq(PHOTO_ID, id));
    List<Row> rows = cassandra.query(select).all();
    Set<String> tags = new HashSet<>();
    rows.forEach(row -> {
      tags.add(row.getString(TAG));
    });
    return new PhotoTagsModel(id, tags);
  }
}
