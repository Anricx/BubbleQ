package com.chinaroad.bubble.adapter;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.chinaroad.foundation.transfer.session.Session;
import com.chinaroad.foundation.transfer.session.SocketSession;
import com.chinaroad.foundation.utils.NumberUtils;

public class SessionAdapter {

	private static final String ___ADDRESS____ = "___ADDRESS____";
	private static final String ___IDENTIFIER____ = "___IDENTIFIER____";
	private static final String ___CONTEXT____ = "___CONTEXT____";
	
	public static void initialize(Session session) {
		InetSocketAddress address = (InetSocketAddress)((SocketSession) session).getRemoteSocketAddress();
		session.setAttribute(___ADDRESS____, address);
	}
	
	public static String generateIdentifier(Session session, String name) {
		String identifier = String.format("%s@%s:%d#%d", 
									name, 
									SessionAdapter.localAddress(session), 
									SessionAdapter.localPort(session), 
									SessionAdapter.generate(name));
		// Inject Client Infor...
		session.setAttribute(___IDENTIFIER____, identifier);
		session.setAttribute(___CONTEXT____, new SessionContext());
		
		return identifier;
	}
	
	public static boolean releaseIdentifier(Session session) {
		String name = SessionAdapter.clientName(session);
		int id = SessionAdapter.clientId(session);
		return SessionAdapter.release(id, name);
	}
	
	public static String getIdentifier(Session session) {
		return session.getAttribute(___IDENTIFIER____) == null ? null : (String) session.getAttribute(___IDENTIFIER____);
	}
	
	public static SessionContext getSessionContext(Session session) {
		if (session.getAttribute(___CONTEXT____) == null) return null;
		return (SessionContext) session.getAttribute(___CONTEXT____);
	}
	
	public static String clientAddress(Session session) {
		if (session.getAttribute(___ADDRESS____) == null) return "NA";
		return session.getAttribute(___ADDRESS____).toString();
	}
	
	private static String clientName(Session session) {
		if (session.getAttribute(___IDENTIFIER____) == null) return null;
		String identifier =  (String) session.getAttribute(___IDENTIFIER____);
		if (identifier.lastIndexOf("@") == -1) {
			return null;
		}
		return identifier.substring(0, identifier.lastIndexOf("@"));
	}
	
	private static int clientId(Session session) {
		if (session.getAttribute(___IDENTIFIER____) == null) return -1;
		String identifier =  (String) session.getAttribute(___IDENTIFIER____);
		return SessionAdapter.clientId(identifier);
	}
	
	private static int clientId(String identifier) {
		if (identifier.lastIndexOf("#") == -1) {
			return -1;
		}
		return NumberUtils.toInt(identifier.substring(identifier.lastIndexOf("#") + 1), -1);
	}
	
	private static String localAddress(Session session) {
		InetSocketAddress locale = (InetSocketAddress) ((SocketSession) session).getLocalSocketAddress();
		return locale.getAddress().getHostAddress();
	}
	
	private static int localPort(Session session) {
		InetSocketAddress locale = (InetSocketAddress) ((SocketSession) session).getLocalSocketAddress();
		return locale.getPort();
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
