package com.smithies.photosorter.model;

public class TagPhotoRequest {

	private String tag;
	private String photoLocation;
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getPhotoLocation() {
		return photoLocation;
	}
	
	public void setPhotoLocation(String photoLocation) {
		this.photoLocation = photoLocation;
	}
}
