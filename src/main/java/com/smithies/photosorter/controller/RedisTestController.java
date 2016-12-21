package com.smithies.photosorter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.smithies.redis.common.RedisTest;

@Controller
@RequestMapping("redis")
public class RedisTestController {

  @Autowired
  private RedisTest redisTest;

  @RequestMapping(method = RequestMethod.GET, value = "test")
  public void create() {
    redisTest.testRedisConnection();
  }
}
