package com.smithies.photosorter.component.filemanager;

import java.util.ArrayList;
import java.util.Arrays;

public interface FileManagerComponent {

	public static final String FOLDER_SORTED = "folder.sorted";
	public static final String FOLDER_INPUT = "folder.input";
	public static final String FOLDER_MAIN = "folder.main";
	public static final String FOLDER_REPO = "folder.repo";
	public static final ArrayList<String> EXTENSIONS = new ArrayList<String>(Arrays.asList(".jpg"));
	
	void createMainFolders();

}
