package com.chinaroad.bubble.adapter;

import java.util.HashSet;
import java.util.Set;

public class SessionContext {

	public Set<String> topics = null;
	public Set<String> listening = null;

	public Set<String> getTopics() {
		return topics;
	}

	public synchronized SessionContext addTopic(String topic) {
		if (topics == null) topics = new HashSet<String>();
		topics.add(topic);
		return this;
	}
	
	public Set<String> getListening() {
		return topics;
	}

	public synchronized SessionContext addListening(String topic) {
		if (listening == null) listening = new HashSet<String>();
		listening.add(topic);
		return this;
	}
	
}
