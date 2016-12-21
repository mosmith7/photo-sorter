package com.smithies.es09.common;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

  @Autowired
  private ElasticSearch09TestDao dao;

  @RequestMapping(method = RequestMethod.POST, value = "save")
  public void save(@RequestBody TestModel model) {
    dao.save(model);
  }

  @RequestMapping(method = RequestMethod.POST, value = "search")
  public List<TestModel> search(@RequestBody TestSearchRequest request) {
    return dao.search(request);
  }

}
