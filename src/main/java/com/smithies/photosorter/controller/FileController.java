package com.smithies.photosorter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.smithies.photosorter.component.filemanager.FileManagerComponent;

@Controller
@RequestMapping("/filemanager")
public class FileController {

  @Autowired
  private FileManagerComponent fileManager;

  @RequestMapping(method = RequestMethod.GET, value = "create")
  public String create() {
    fileManager.createMainFolders();
    return "test";
  }

  @RequestMapping("/test")
  public ModelAndView test() {
    ModelAndView modelAndView = new ModelAndView("index");
    return modelAndView;
  }
}
