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

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

@Component
public class TagsToPhotosCassandraDao {

  Logger log = LoggerFactory.getLogger(this.getClass());

  // Keyspace and table
  private static final String KS = "photosorter";
  private static final String TAGS_TO_PHOTOS_TABLE = "tags_to_photos";
  private static final String TAGS_TO_PHOTOS = KS + "." + TAGS_TO_PHOTOS_TABLE;

  // Schema
  private static final String PHOTO_ID = "photoId";
  private static final String TAG = "tag";
  private static final String PARTITION_KEY = TAG;

  // SQL statements
  private static final String CQL_CREATE =
      String.format("CREATE TABLE IF NOT EXISTS %s (%s text, %s uuid, PRIMARY KEY(%s))",
          TAGS_TO_PHOTOS, TAG, PHOTO_ID, PARTITION_KEY);
  private static final String INSERT_CQL =
      String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)", TAGS_TO_PHOTOS, TAG, PHOTO_ID);

  private CassandraOperations cassandra;

  @Autowired
  public void setCassandra(CassandraOperations cassandra) {
    this.cassandra = cassandra;
    cassandra.execute(CQL_CREATE);
  }

  public TagToPhotoIdsModel add(final AddOrRemovePhotoTagRequest request) {
    cassandra.execute(new SimpleStatement(INSERT_CQL, request.getTag(), request.getPhotoId()));
    return get(request.getTag());
  }

  // public TagToPhotoIdsModel remove(final AddOrRemovePhotoTagRequest request) {
  // Delete delete = QueryBuilder.delete().from(KS, TAGS_TO_PHOTOS_TABLE);
  // delete.where(eq(TAG, request.getTag())).and(eq(PHOTO_ID, request.getPhotoId()));
  // cassandra.execute(delete);
  // return get(request.getTag());
  // }

  public TagToPhotoIdsModel get(String tag) {
    Select select = QueryBuilder.select().all().from(TAGS_TO_PHOTOS_TABLE);
    select.where(eq(TAG, tag));
    List<Row> rows = cassandra.query(select).all();
    Set<UUID> photoIds = new HashSet<>();
    rows.forEach(row -> {
      photoIds.add(row.getUUID(PHOTO_ID));
    });
    return new TagToPhotoIdsModel(tag, photoIds);
  }

  // public Set<String> addTag(final String tag) {
  // Set<String> allTags = getAllTags();
  // if (!allTags.contains(tag)) {
  // TagToPhotoIdsModel object = new TagToPhotoIdsModel(tag);
  // Set<UUID> photoIds = new HashSet<>();
  // photoIds.add(UUID.randomUUID());
  // object.setPhotoIds(photoIds);
  // final String json = mapper.marshall(object);
  // cassandra.execute(new SimpleStatement(INSERT_CQL, tag, json));
  // } else {
  // log.warn("Tried to add tag {} but it already exists", tag);
  // }
  // return getAllTags();
  // }

  public Set<String> getAllTags() {
    ResultSet query = cassandra.query(String.format("SELECT * from %s", TAGS_TO_PHOTOS));
    List<Row> all = query.all();
    Set<String> tags = new HashSet<>();
    all.forEach(row -> {
      String tag = row.getString(TAG);
      tags.add(tag);
    });
    return tags;
  }
}
