package com.chinaroad.bubble.context;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.chinaroad.foundation.transfer.session.Session;
import com.chinaroad.foundation.utils.ArrayUtils;

public class BubbleManager {
	
	private static final Session[] EMPTY_SESSION_ARRAY = new Session[0];
	private static final Map<String, LinkedList<Session>> CLIENT_MAP = new HashMap<String, LinkedList<Session>>();	/* identifier => session */
	private static final Map<String, Session> CLIENTS = new HashMap<String, Session>();	/* identifier => session */
	
	public static String signin(String name, String identifier, Session session) {
		synchronized (Locker.FOR_SIGNIN) {
			if (!CLIENT_MAP.containsKey(name)) CLIENT_MAP.put(name, new LinkedList<Session>());
	
			CLIENTS.put(identifier, session);
			CLIENT_MAP.get(name).add(session);
		}
		return identifier;
	}
	
	public static Session getByIdentifier(String identifier) {
		return CLIENTS.get(identifier);
	}
	
	public static String[] findAllClients(String name) {
		synchronized (Locker.FOR_SIGNIN) {
			if (!CLIENT_MAP.containsKey(name)) return ArrayUtils.EMPTY_STRING_ARRAY;
			return SessionManager.getIdentifiers(CLIENT_MAP.get(name));
		}
	}
	
	public static Session[] findAllClientSessions(String name) {
		synchronized (Locker.FOR_SIGNIN) {
			if (!CLIENT_MAP.containsKey(name)) return EMPTY_SESSION_ARRAY;
			LinkedList<Session> clients = CLIENT_MAP.get(name);
			return clients.toArray(new Session[clients.size()]);
		}
	}
	
	public static Session selectClient(String name) {
		synchronized (Locker.FOR_SIGNIN) {
			if (!CLIENT_MAP.containsKey(name)) return null;
			LinkedList<Session> clients = CLIENT_MAP.get(name);
			if (clients.size() == 0) return null;
			if (clients.size() == 1) return clients.peek();
			
			//TODO Add SLB...
			Session client = clients.poll();	// %Loop First To Select
			clients.offer(client);	// %Add To Last
			return client;
		}
	}
	
	public static String signout(Session session) {	
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
		return context.getIdentifier();
	}
	
}
