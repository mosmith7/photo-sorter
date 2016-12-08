package com.smithies.photosorter.component.sorter;

import java.util.List;
import java.util.UUID;

public interface PhotoSorterComponent {

	PhotosToSort getImageFilename(String directory, List<String> extensions);
	
	String convertToBase64(String filename);

	String renameWithId(String location, UUID id);

}
