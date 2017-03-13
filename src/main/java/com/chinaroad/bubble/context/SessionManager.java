package com.chinaroad.bubble.context;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.chinaroad.foundation.transfer.session.Session;
import com.chinaroad.foundation.transfer.session.SocketSession;

public class SessionManager {

	private static final String ___CONTEXT____ = "___CONTEXT____";
	
	public static void initialize(Session session) {
		InetSocketAddress remoteAddress = (InetSocketAddress)((SocketSession) session).getRemoteSocketAddress();
		InetSocketAddress localAddress = (InetSocketAddress)((SocketSession) session).getLocalSocketAddress();
		session.setAttribute(___CONTEXT____, new SessionContext(localAddress, remoteAddress));
	}
	
	public static SessionContext getContext(Session session) {
		if (session.getAttribute(___CONTEXT____) == null) return null;
		return (SessionContext) session.getAttribute(___CONTEXT____);
	}
	
	public static String[] getIdentifiers(List<Session> sessions) {
		Set<String> set = new HashSet<String>();
		for (Session session : sessions) {
			set.add(SessionManager.getContext(session).getIdentifier());
		}
		return set.toArray(new String[set.size()]);
	}
	
	public static String register(Session session, String name) {
		if (name.contains("@") || name.contains("#")) throw new IllegalArgumentException("`name` contains '@' or '#'!");
	
		// Inject Client Infor...
		String identifier = SessionManager.getContext(session).initialize(name);
		BubbleManager.signin(name.toString(), identifier, session); // #Signin Client~~~
		return identifier;
	}
	
	public static void close(Session session) {
		BubbleManager.signout(session); // #Signout Client~~~
		// Destory Context...
		SessionManager.getContext(session).destory();
	}
	
	private volatile static Map<String, Set<Integer>> ids = new ConcurrentHashMap<String, Set<Integer>>();
	
	/**
	 * generate next client idx.
	 * @param forWho
	 * @return
	 */
	protected synchronized static int generate(String forWho) {
		if (!ids.containsKey(forWho)) {
			Set<Integer> set = new HashSet<Integer>();
			ids.put(forWho, set);
		}
		Set<Integer> using = ids.get(forWho);
		int id = -1;
		for (int i = 1; i < Integer.MAX_VALUE; i++) {
			if (using.contains(i)) continue;
			id = i; break;
		}
		using.add(id);
		return id;
	}
	
	/**
	 * release id to pool.
	 * @param id
	 * @param forWho
	 * @return
	 */
	protected synchronized static boolean release(int id, String forWho) {
		if (id < 1) return false;
		if (!ids.containsKey(forWho)) return false;
		return ids.get(forWho).remove(id);
	}
	
}
