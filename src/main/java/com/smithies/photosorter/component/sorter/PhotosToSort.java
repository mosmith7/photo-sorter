package com.smithies.photosorter.component.sorter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PhotosToSort {

	private AtomicInteger index = new AtomicInteger(0);
	
	private List<String> photoNames = new ArrayList<>();
	
	private String currentPhotoName;

	public AtomicInteger getIndex() {
		return this.index;
	}

	public void setIndex(AtomicInteger index) {
		this.index = index;
	}
	
	public void setCurrentPhotoName(String currentPhotoName) {
		this.currentPhotoName = currentPhotoName;
	}
	
	public String getCurrentPhotoName() {
		return this.currentPhotoName;
	}

	public List<String> getPhotoNames() {
		return photoNames;
	}
	
	public List<String> addPhotoName(final String photoName) {
		photoNames.add(photoName);
		return photoNames;
	}

	public void setPhotoNames(List<String> photoNames) {
		this.photoNames = photoNames;
		this.currentPhotoName = photoNames.get(index.get());
	}
	
	public String next() {
		setCurrentPhotoName(photoNames.get(this.index.incrementAndGet()));
		return getCurrentPhotoName();
	}
	
	public String previous() {
		setCurrentPhotoName(photoNames.get(this.index.decrementAndGet()));
		return getCurrentPhotoName();
	}
	
}
