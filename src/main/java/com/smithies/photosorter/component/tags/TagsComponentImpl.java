package com.smithies.photosorter.component.tags;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class TagsComponentImpl implements TagsComponent {

  @Autowired
  PhotosToTagsCassandraDao photosToTagsDao;

  @Autowired
  TagsToPhotosCassandraDao tagsToPhotosDao;

  @Override
  public Set<String> getAllTags() {
    return tagsToPhotosDao.getAllTags();
  }

  @Override
  public void addPhotoTag(AddOrRemovePhotoTagRequest request) {
    photosToTagsDao.addPhotoTag(request);
    tagsToPhotosDao.add(request);
  }

  @Override
  public PhotoTagsModel getPhotoTags(UUID photoId) {
    return photosToTagsDao.get(photoId);
  }

  @Override
  public TagToPhotoIdsModel getIdsForTag(String tag) {
    return tagsToPhotosDao.get(tag);
  }

}
