package com.smithies.photosorter.component.sorter;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.smithies.photosorter.component.filemanager.FileManagerComponent;

@Component
class PhotoSorterComponentImpl implements PhotoSorterComponent {
	
	@Autowired
	private Environment env;

	@Override
	public PhotosToSort getImageFilename(final String directory, final List<String> extensions) {
		PhotosToSort photos = new PhotosToSort();
		
		File folder = new File(directory);
		File[] listOfFiles = folder.listFiles();
		boolean photoSet = false;
		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
			  String filename = listOfFiles[i].getName();
			  for (int j=0; j<extensions.size(); j++) {
				  if (StringUtils.hasText(extensions.get(j))) {
					  if (!photoSet) {
						  photos.setCurrentPhotoName(filename);
						  photoSet = true;
					  }
					  photos.addPhotoName(filename);
		    	}
			  }
		   }
		}
		return photos;
	}
	
	@Override
	public String convertToBase64(final String filename) {
		File file = new File(filename);
		byte[] imageBytes = new byte[102400];
		try {
			imageBytes = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String base64String = Base64.getEncoder().encodeToString(imageBytes);
		return base64String;
	}
	
	@Override
	public String renameWithId(final String location, final UUID id) {
		String newDirectoryLocation = env.getProperty(FileManagerComponent.FOLDER_REPO);
		String newLocation = newDirectoryLocation + "/" + id.toString() + ".jpg";
		File imageFile = new File(location);
		imageFile.renameTo(new File(newLocation));
		return newLocation;
	}
	
}
