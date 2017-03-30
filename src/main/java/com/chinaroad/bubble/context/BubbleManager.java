package com.chinaroad.bubble.context;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.chinaroad.foundation.transfer.session.Session;

public class BubbleManager {
	
	private static Map<String, LinkedList<Session>> CLIENT_MAP = new ConcurrentHashMap<String, LinkedList<Session>>();	/* identifier => session */
	private static Map<String, Session> CLIENTS = new ConcurrentHashMap<String, Session>();	/* identifier => session */
	
	public static void signin(String name, String identifier, Session session) {
		synchronized (Locker.FOR_SIGNIN) {
			CLIENTS.put(identifier, session);
			
			if (!CLIENT_MAP.containsKey(name)) {
				CLIENT_MAP.put(name, new LinkedList<Session>());
			}
			CLIENT_MAP.get(name).add(session);
		}
	}
	
	public static Session getByIdentifier(String identifier) {
		return CLIENTS.get(identifier);
	}
	
	public static Session selectClient(String name) {
		// System.out.println("selectClient##########");
		// System.out.println(name);
		if (!CLIENT_MAP.containsKey(name)) return null;
		
		LinkedList<Session> clients = CLIENT_MAP.get(name);
		//TODO Add SLB...
		switch (clients.size()) {
		case 1:
			return clients.peek();

		default:
			Session client = clients.poll();	// %Loop First To Select
			clients.offer(client);	// %Add To Last
			return client;
		}
	}
	
	public static void signout(Session session) {	
		SessionContext context = SessionManager.getContext(session);
		// $Release Topics...
		if (context.getTopics() != null) {
			Set<String> topics = context.getTopics();
			for (String topic : topics) {
				if (!TopicManager.TOPICS.containsKey(topic)) continue;
				TopicManager.TOPICS.get(topic).remove(session);
			}
		}
		// $Release Listening...
		if (context.getListening() != null) {
			Set<String> topics = context.getListening();
			for (String topic : topics) {
				if (!TopicManager.LISTENERS.containsKey(topic)) continue;
				TopicManager.LISTENERS.get(topic).remove(session);
			}
		}
		// #Release Client~~~
		if (context.getIdentifier() != null) {
			synchronized (Locker.FOR_SIGNIN) {
				CLIENTS.remove(context.getIdentifier());
				CLIENT_MAP.get(context.getName()).remove(session);
			}
		}
	}
	
}
