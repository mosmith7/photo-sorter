package com.smithies.photosorter.component.tags;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.RowMapper;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.smithies.cassandra.common.CqlCommonFunctions;
import com.smithies.jackson.common.FasterObjectMapper;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

@Component
public class TagsToPhotosCassandraDao {
	
	 Logger log = LoggerFactory.getLogger(this.getClass());

	 private static final String KS = "photosorter";

	 private static final String TAGS_TO_PHOTOS_TABLE = "tags_to_photos";

	 private static final String TAGS_TO_PHOTOS = KS + "." + TAGS_TO_PHOTOS_TABLE;

	 private static final String CQL_CREATE = String.format("CREATE TABLE IF NOT EXISTS %s (tag text, tagToPhotos text, PRIMARY KEY(tag))",TAGS_TO_PHOTOS);

	 private static final String INSERT_CQL = String.format("INSERT INTO %s (tag, tagToPhotos) VALUES (?, ?)", TAGS_TO_PHOTOS);
	  
	 private CassandraOperations cassandra;
	 
	 private final FasterObjectMapper mapper;

	 private final RowMapper<TagToPhotoIdsModel> rowMap;

	 /**
	  * @param mapper the mapper that will be used in serialising TagToPhotoIdsModel
	  */
	 @Autowired
	 public TagsToPhotosCassandraDao(FasterObjectMapper mapper) {
	   this.mapper = mapper;
	   this.rowMap = CqlCommonFunctions.mapperFor(mapper, "tagToPhotos", TagToPhotoIdsModel.class);
	 }
	 
	 @Autowired
	 public void setCassandra(CassandraOperations cassandra) {
	   this.cassandra = cassandra;
	   cassandra.execute(CQL_CREATE);
	 }
	 
	 public void savePhotoTags(final TagToPhotoIdsModel tagToPhotos) {
		final String json = mapper.marshall(tagToPhotos);
		cassandra.execute(new SimpleStatement(INSERT_CQL, tagToPhotos.getTag(), json));
	}

	public void addPhotoTags(final AddOrRemovePhotoTagRequest request) {
		TagToPhotoIdsModel photoIds = getPhotoIds(request.getTag()).orElse(new TagToPhotoIdsModel(request.getTag()));
		photoIds.getPhotoIds().add(request.getPhotoId());
		final String json = mapper.marshall(photoIds);
		cassandra.execute(new SimpleStatement(INSERT_CQL, photoIds.getTag(), json));
	}
	
	public void removePhotoTags(final AddOrRemovePhotoTagRequest request) {
		TagToPhotoIdsModel photoIds = getPhotoIds(request.getTag()).orElse(new TagToPhotoIdsModel(request.getTag()));
		photoIds.getPhotoIds().remove(request.getPhotoId());
		final String json = mapper.marshall(photoIds);
		cassandra.execute(new SimpleStatement(INSERT_CQL, photoIds.getTag(), json));
	}
	 
	public Optional<TagToPhotoIdsModel> getPhotoIds(String tag) {
		Select select = QueryBuilder.select().all().from(TAGS_TO_PHOTOS_TABLE);
		select.where(eq("tag", tag));
		Optional<TagToPhotoIdsModel> test = CqlCommonFunctions.getOptional(cassandra, rowMap, select);
		return test;
	}
	
	public Set<String> addTag(final String tag) {
		Set<String> allTags = getAllTags();
		if (!allTags.contains(tag)) {
			TagToPhotoIdsModel object = new TagToPhotoIdsModel(tag);
			Set<UUID> photoIds = new HashSet<>();
			photoIds.add(UUID.randomUUID());
			object.setPhotoIds(photoIds);
			final String json = mapper.marshall(object);
			cassandra.execute(new SimpleStatement(INSERT_CQL, tag, json));
		} else {
			log.warn("Tried to add tag {} but it already exists", tag);
		}
		return getAllTags();
	}
	
	public Set<String> getAllTags() {
		ResultSet query = cassandra.query(String.format("SELECT * from %s", TAGS_TO_PHOTOS));
		List<Row> all = query.all();
		Set<String> tags = new HashSet<>();
		all.forEach(row -> {
			String tag = row.getString(0);
			tags.add(tag);
		});
		return tags;
	}
}
