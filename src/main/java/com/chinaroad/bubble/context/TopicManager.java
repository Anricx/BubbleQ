package com.chinaroad.bubble.context;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.chinaroad.foundation.transfer.session.Session;

public class TopicManager {

	protected static Map<String, LinkedList<Session>> TOPICS = new ConcurrentHashMap<String, LinkedList<Session>>();
	protected static Map<String, LinkedList<Session>> LISTENERS = new ConcurrentHashMap<String, LinkedList<Session>>();
	
	public static void subscribe(Session session, String topic) {
		synchronized (Locker.FOR_TOPIC) {
			if (!TOPICS.containsKey(topic)) {
				TOPICS.put(topic, new LinkedList<Session>());
			}
			if (!TOPICS.get(topic).contains(session)) {
				TOPICS.get(topic).offer(session);	// Let Global Server Know!
				SessionManager.getContext(session).addTopic(topic);	// Let Current Session Know!
			}
		}
	}
	
	public static boolean hasCustomer(String topic) {
		return TOPICS.containsKey(topic);
	}
	
	public static LinkedList<Session> getAllListeners(String topic) {
		return TOPICS.get(topic);
	}
	
	public static LinkedList<Session> getAllCustomers(String topic) {
		return TOPICS.get(topic);
	}
	
	public static Session getCustomer(String topic) {
		if (!TOPICS.containsKey(topic)) return null;

		LinkedList<Session> customers = TOPICS.get(topic);
		//TODO Add SLB...
		switch (customers.size()) {
		case 1:
			return customers.peek();

		default:
			Session client = customers.poll();	// %Loop First To Select
			customers.offer(client);	// %Add To Last
			return client;
		}
	}

	public static void listening(Session session, String topic) {
		synchronized (Locker.FOR_LISTENER) {
			if (!LISTENERS.containsKey(topic)) {
				LISTENERS.put(topic, new LinkedList<Session>());
			}
			if (!LISTENERS.get(topic).contains(session)) {
				LISTENERS.get(topic).offer(session);	// Let Global Server Know!
				SessionManager.getContext(session).addListening(topic);	// Let Current Session Know!
			}
		}
	}
	
	public static boolean hasListener(String topic) {
		return LISTENERS.containsKey(topic);
	}
	
}
