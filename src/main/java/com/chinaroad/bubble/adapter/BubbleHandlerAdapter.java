package com.chinaroad.bubble.adapter;

import java.util.Arrays;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaroad.bubble.Application;
import com.chinaroad.foundation.transfer.handler.HandlerAdapter;
import com.chinaroad.foundation.transfer.session.IdleStatus;
import com.chinaroad.foundation.transfer.session.Session;

public class BubbleHandlerAdapter extends HandlerAdapter {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void sessionOpened(Session session) throws Exception {
		SessionAdapter.initialize(session);
		// session.setIdleTime(IdleStatus.READ_IDLE, AUTH_TIMEOUT);
		logger.info("[Bubble][O][" + SessionAdapter.clientAddress(session) + "] - Connection Established!");
	}

	@Override
	public void sessionIdle(Session session, IdleStatus status)
			throws Exception {
		logger.debug("[Bubble][I][" + SessionAdapter.clientAddress(session) + "] - Connection IDLE:" + status + ".");
	}

	@Override
	public void sessionClosed(Session session) throws Exception {
		SessionAdapter.releaseIdentifier(session);
		logger.warn("[Bubble][C][" + SessionAdapter.clientAddress(session) + "] - Connection LOST!");
		// $Release Topics...
		if (SessionAdapter.getSessionContext(session) != null && SessionAdapter.getSessionContext(session).getTopics() != null) {
			Set<String> topics = SessionAdapter.getSessionContext(session).getTopics();
			for (String topic : topics) {
				if (!Application.TOPICS.containsKey(topic)) continue;
				Application.TOPICS.get(topic).remove(session);
			}
		}
		// $Release Listening...
		if (SessionAdapter.getSessionContext(session) != null && SessionAdapter.getSessionContext(session).getListening() != null) {
			Set<String> topics = SessionAdapter.getSessionContext(session).getListening();
			for (String topic : topics) {
				if (!Application.LISTENERS.containsKey(topic)) continue;
				Application.LISTENERS.get(topic).remove(session);
			}
		}
		// #Release Client~~~
		String identifier = SessionAdapter.getIdentifier(session); 
		if (identifier != null) Application.CLIENTS.remove(identifier);
	}
	
	@Override
	public void exceptionCaught(Session session, Throwable cause) {
		logger.error("[Bubble][E][" + SessionAdapter.clientAddress(session) + "] - Connection ERROR:", cause);
	}

	@Override
	public void dataNotSent(Session session, Object data) throws Exception {
		logger.error("[Bubble][E][" + SessionAdapter.clientAddress(session) + "] - Payload Not Send:" + Arrays.toString((byte[]) data) + ".");
	}

	@Override
	public void dataSent(Session session, Object data) throws Exception {
		logger.debug("[Bubble][W][" + SessionAdapter.clientAddress(session) + "] - Payload Sent:" + Arrays.toString((byte[]) data) + ".");
	}
	
}