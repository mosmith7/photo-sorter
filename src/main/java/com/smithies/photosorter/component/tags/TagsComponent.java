package com.smithies.photosorter.component.tags;

import java.util.Set;
import java.util.UUID;

public interface TagsComponent {

  PhotoTagsModel getPhotoTags(UUID photoId);

  Set<String> getAllTags();

  void addPhotoTag(AddOrRemovePhotoTagRequest request);

  TagToPhotoIdsModel getIdsForTag(String tag);

}
