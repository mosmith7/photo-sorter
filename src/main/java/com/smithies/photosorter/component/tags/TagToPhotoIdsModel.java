package com.smithies.photosorter.component.tags;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TagToPhotoIdsModel {

	private String tag;
	
	private Set<UUID> photoIds;
	
	public TagToPhotoIdsModel() {
		this.tag = "";
		this.photoIds = new HashSet<>();
	}
	
	public TagToPhotoIdsModel(String tag) {
		this.tag = tag;
		this.photoIds = new HashSet<>();
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Set<UUID> getPhotoIds() {
		return photoIds;
	}

	public void setPhotoIds(Set<UUID> photoIds) {
		this.photoIds = photoIds;
	}
}
