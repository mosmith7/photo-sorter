package com.smithies.photosorter.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.smithies.photosorter.component.filemanager.FileManagerComponent;
import com.smithies.photosorter.component.sorter.PhotoSorterComponent;
import com.smithies.photosorter.component.sorter.PhotosToSort;
import com.smithies.photosorter.model.RenamePhotoRequest;

@Controller
@RequestMapping("/api/sorter")
public class SorterController {

	Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private Environment env;
	
	@Autowired
	private PhotoSorterComponent sorter;
	
	private PhotosToSort photosToSort;
	
	// Get all photos
	@RequestMapping(method = RequestMethod.GET, value = "getAll")
	public ModelAndView getAll() {
		// Get next photo
		photosToSort = sorter.getImageFilename(env.getProperty(FileManagerComponent.FOLDER_INPUT), FileManagerComponent.EXTENSIONS);
		
		return getMainPage();
	}

	// Endpoint to view next photo to be sorted
	@RequestMapping(method = RequestMethod.GET, value = "next")
	@ResponseBody
	public ModelAndView getNext() {
		// Get next photo
		if (photosToSort.getPhotoNames().isEmpty()) {
			log.error("Photos need to be loaded.");
		}
		if (photosToSort.getIndex().get()<photosToSort.getPhotoNames().size() - 1) {
			photosToSort.next();
		}
		
		return getMainPage();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "previous")
	@ResponseBody
	public ModelAndView getPrevious() {
		// Get next photo
		if (photosToSort.getPhotoNames().isEmpty()) {
			log.error("Photos need to be loaded.");
		}
		if (photosToSort.getIndex().get()!=0) {
			photosToSort.previous();
		}
		
		return getMainPage();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "rename")
	public UUID renameAndMove(@RequestBody RenamePhotoRequest request) {
		if (request.getPhotoLocation().contains(env.getProperty(FileManagerComponent.FOLDER_INPUT))) {
			UUID id = UUID.randomUUID();
			sorter.renameWithId(request.getPhotoLocation(), id);
			return id;
		}
		throw new RuntimeException();
	}
	
	private ModelAndView getMainPage() {
		// jsp and html files in webapp folder can automatically be seen. Files in WEB-INF can't.
		// I can't get the prefix and suffix to work in spring-dispatcher-servlet.xml so atm not putting files under WEB-INF
		ModelAndView modelAndView = new ModelAndView("/mainPage.jsp"); // view name 
		String filename = env.getProperty(FileManagerComponent.FOLDER_INPUT) + "/" + photosToSort.getCurrentPhotoName();
		String base64 = sorter.convertToBase64(filename);
				
		modelAndView.addObject("location", filename);
		modelAndView.addObject("base64", base64);
				
		return modelAndView;
	}
}
