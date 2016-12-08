package com.smithies.photosorter.controller;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.smithies.photosorter.component.tags.AddOrRemovePhotoTagRequest;
import com.smithies.photosorter.component.tags.PhotoTagsModel;
import com.smithies.photosorter.component.tags.TagsComponent;
import com.smithies.photosorter.model.RenamePhotoRequest;
import com.smithies.photosorter.model.TagPhotoRequest;

@RestController
@RequestMapping("/api/tags")
public class TagsController {

  @Autowired
  private SorterController sorterController;

  @Autowired
  private TagsComponent tags;

  // @RequestMapping(method = RequestMethod.POST, value = "add")
  // public void add(@RequestBody AddTagRequest request) {
  // tags.addTag(request.getTag());
  // }

  @RequestMapping(method = RequestMethod.GET, value = "")
  public Set<String> getAll() {
    return tags.getAllTags();
  }

  @RequestMapping(method = RequestMethod.POST, value = "image/add")
  public void addTagsToImage(@RequestBody TagPhotoRequest request) {
    RenamePhotoRequest renameRequest = new RenamePhotoRequest();
    renameRequest.setPhotoLocation(request.getPhotoLocation());
    // rename and move photo if it is in input folder i.e. hasn't been given UUID or tags
    UUID id = sorterController.renameAndMove(renameRequest);
    AddOrRemovePhotoTagRequest addRequest = new AddOrRemovePhotoTagRequest();
    addRequest.setPhotoId(id);
    addRequest.setTag(request.getTag());
    tags.addPhotoTag(addRequest);
  }

  @RequestMapping(method = RequestMethod.POST, value = "image/{photoId}/add")
  public void addTagsToImage(@PathVariable("photoId") UUID photoId,
      @RequestBody TagPhotoRequest request) {
    AddOrRemovePhotoTagRequest addRequest = new AddOrRemovePhotoTagRequest();
    addRequest.setPhotoId(photoId);
    addRequest.setTag(request.getTag());
    tags.addPhotoTag(addRequest);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/{photoId}")
  public PhotoTagsModel getTagsForImage(@PathVariable("photoId") UUID photoId) {
    return tags.getPhotoTags(photoId);
  }

}
