package com.chinaroad.bubble.biz;

import java.util.Arrays;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaroad.bubble.cache.RedisCache;

@Deprecated
public class StatsBiz {

	private static StatsBiz _instance = null;
	
	public static synchronized StatsBiz initialize() {
		synchronized (StatsBiz.class) {
			if (_instance != null) return _instance;
			
			_instance = new StatsBiz();
			_instance.prepare();
		}
		return _instance;
	}
	
	public static synchronized StatsBiz getInstance() {
		synchronized (StatsBiz.class) {
			if (_instance == null) throw new IllegalStateException("object not initialized!");
		}
		return _instance;
	}
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private RedisCache conn;
	
	public StatsBiz() {
		conn = new RedisCache();
	}

	public void prepare() {
		logger.info("[Stats][Prepare] - DEL Bad Cache Links...");
		Set<String> keys = conn.keys("link:*");
		if (keys == null || keys.size() == 0) {
			logger.info("[Stats][Prepare] - No Bad Cache Link!");
			return;
		}

		logger.info("[Stats][Prepare] - DEL Links:\n" + Arrays.toString(keys.toArray(new String[keys.size()])));
		for (String key : keys) {
			conn.del(key);
		}
		logger.info("[Stats][Prepare] - DEL Successfully!");
	}

	public void triggerCreate(String key, String value) {
		conn.put(key, value);
	}

	public void triggerDestory(String key) {
		conn.del(key);
	}
	
}
