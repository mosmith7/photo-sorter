package com.smithies.photosorter.component.tags;

import java.util.Set;
import java.util.UUID;

public interface TagsComponent {

	void savePhotoTags(PhotoTagsModel model);

	PhotoTagsModel getPhotoTags(UUID photoId);

	Set<String> getAllTags();

	Set<String> addTag(String tag);

	void addPhotoTag(AddOrRemovePhotoTagRequest request);

}
