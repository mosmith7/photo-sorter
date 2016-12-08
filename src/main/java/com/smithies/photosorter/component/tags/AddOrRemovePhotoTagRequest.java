package com.smithies.photosorter.component.tags;

import java.util.UUID;

public class AddOrRemovePhotoTagRequest {

	private UUID photoId;
	
	private String tag;

	public UUID getPhotoId() {
		return photoId;
	}

	public void setPhotoId(UUID photoId) {
		this.photoId = photoId;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}
