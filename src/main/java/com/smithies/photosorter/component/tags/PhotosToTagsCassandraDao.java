package com.smithies.photosorter.component.tags;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.RowMapper;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.smithies.cassandra.common.CqlCommonFunctions;
import com.smithies.jackson.common.FasterObjectMapper;

@Component
class PhotosToTagsCassandraDao {

	 private static final String KS = "photosorter";

	 private static final String TAGS_TO_PHOTOS_TABLE = "photos_to_tags";

	 private static final String PHOTO_TO_TAGS = KS + "." + TAGS_TO_PHOTOS_TABLE;

	 private static final String CQL_CREATE = String.format("CREATE TABLE IF NOT EXISTS %s (id uuid, photoTags text, PRIMARY KEY(id))",PHOTO_TO_TAGS);

	 private static final String INSERT_CQL = String.format("INSERT INTO %s (id, photoTags) VALUES (?, ?)", PHOTO_TO_TAGS);
	  
	 private CassandraOperations cassandra;

	 private final FasterObjectMapper mapper;

	 private final RowMapper<PhotoTagsModel> rowMap;

	 /**
	  * @param mapper the mapper that will be used in serialising PhotoTagsModel
	  */
	 @Autowired
	 public PhotosToTagsCassandraDao(FasterObjectMapper mapper) {
	   this.mapper = mapper;
	   this.rowMap = CqlCommonFunctions.mapperFor(mapper, "photoTags", PhotoTagsModel.class);
	 }

	 @Autowired
	 public void setCassandra(CassandraOperations cassandra) {
	   this.cassandra = cassandra;
	   cassandra.execute(CQL_CREATE);
	 }
	 
	 public void savePhotoTags(PhotoTagsModel model) {
		final String json = mapper.marshall(model);
		cassandra.execute(new SimpleStatement(INSERT_CQL, model.getPhotoId(), json));
	 }
	 
	 public void addPhotoTag(AddOrRemovePhotoTagRequest request) {
		 PhotoTagsModel photoTags = get(request.getPhotoId()).orElse(new PhotoTagsModel(request.getPhotoId()));
		 photoTags.getTags().add(request.getTag());
		 final String json = mapper.marshall(photoTags);
		 cassandra.execute(new SimpleStatement(INSERT_CQL, photoTags.getPhotoId(), json));
	}
	 
	 public void removePhotoTag(AddOrRemovePhotoTagRequest request) {
		 PhotoTagsModel photoTags = get(request.getPhotoId()).orElse(new PhotoTagsModel());
		 photoTags.getTags().remove(request.getTag());
		 final String json = mapper.marshall(photoTags);
		 cassandra.execute(new SimpleStatement(INSERT_CQL, photoTags.getPhotoId(), json));
	}

	 public Optional<PhotoTagsModel> get(UUID id) {
		 Select select = QueryBuilder.select().all().from(TAGS_TO_PHOTOS_TABLE);
			select.where(eq("id", id));
		 return CqlCommonFunctions.getOptional(cassandra, rowMap, select);
	 }
}
