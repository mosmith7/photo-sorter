package com.smithies.redis.common;

import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;

@Service
public class RedisTest {

  public void testRedisConnection() {
    try {
      Jedis jedis = new Jedis("localhost");
      System.out.println("Connection Successful");
      System.out.println("Server ping: .. " + jedis.ping());

    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
