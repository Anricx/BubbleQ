package com.chinaroad.bubble.context;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

public class SessionContext {

	private String name;
	private String identifier;
	private Integer id;
	
	private InetSocketAddress localeAddress;
	private InetSocketAddress remoteAddress;

	private Set<String> topics = null;
	private Set<String> listening = null;

	public SessionContext(InetSocketAddress localeAddress, InetSocketAddress remoteAddress) {
		super();
		this.localeAddress = localeAddress;
		this.remoteAddress = remoteAddress;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

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

	public InetSocketAddress getLocaleAddress() {
		return localeAddress;
	}

	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public String initialize(String name) {
		this.name = name;
		this.id = SessionManager.generate(name);
		this.identifier = String.format("%s@%s:%d#%d", 
									name, 
									localeAddress.getAddress().getHostAddress(), 
									localeAddress.getPort(), 
									id);
		return this.identifier;
	}
	
	protected void destory() {
		if (this.id != null) SessionManager.release(id, name);
	}
	
	public static boolean isIdentifier(String str) {
		return str.contains("@");
	}
	
}
