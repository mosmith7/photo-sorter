package com.smithies.photosorter.component.filemanager;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
class FileManagerComponentImpl implements FileManagerComponent {

	@Autowired
	private Environment env;
	
	@Override
	public void createMainFolders() {
		createDirectory(env.getProperty(FOLDER_MAIN));
		createDirectory(env.getProperty(FOLDER_INPUT));
		createDirectory(env.getProperty(FOLDER_SORTED));
		createDirectory(env.getProperty(FOLDER_REPO));
	}

	private void createDirectory(final String directoryLocation) {
		File file = new File(directoryLocation);
		if (!file.exists()) {
			if (file.mkdir()) {
				System.out.println("Directory created: " + directoryLocation);
			} else {
				System.out.println("Failed to create directory:" + directoryLocation);
			}
		}
	}
}
