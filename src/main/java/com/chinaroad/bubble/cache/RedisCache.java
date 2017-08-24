package com.chinaroad.bubble.cache;

import java.util.Set;

import redis.clients.jedis.Jedis;

public class RedisCache {

	private Jedis conn;
	
	public RedisCache() {
		conn = new Jedis("120.24.253.84", 9001);
		conn.auth("bvQwxPilqVLd");
	}
	
	public Set<String> keys(String pattern) {
		return conn.keys(pattern);
	}
	
	public Long del(String... key) {
		return conn.del(key);
	}
	
	public String put(String key, String value) {
		return conn.set(key, value);
	}
	
}
