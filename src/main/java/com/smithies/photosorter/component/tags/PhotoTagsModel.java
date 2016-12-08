package com.smithies.photosorter.component.tags;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PhotoTagsModel {

	private UUID photoId;
	
	private Set<String> tags = new HashSet<>();

	public PhotoTagsModel() {
		this.tags = new HashSet<>();
	}
	
	public PhotoTagsModel(UUID photoId) {
		this.photoId = photoId;
		this.tags = new HashSet<>();
	}
	
	public PhotoTagsModel(UUID photoId, Set<String> tags) {
		this.photoId = photoId;
		this.tags = tags;
	}
	
	public UUID getPhotoId() {
		return photoId;
	}

	public void setPhotoId(UUID photoId) {
		this.photoId = photoId;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}
}
