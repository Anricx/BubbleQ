package com.chinaroad.bubble.adapter;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaroad.bubble.context.SessionManager;
import com.chinaroad.foundation.transfer.handler.HandlerAdapter;
import com.chinaroad.foundation.transfer.session.IdleStatus;
import com.chinaroad.foundation.transfer.session.Session;

public class BubbleHandlerAdapter extends HandlerAdapter {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void sessionOpened(Session session) throws Exception {
		SessionManager.initialize(session);
		// session.setIdleTime(IdleStatus.READ_IDLE, AUTH_TIMEOUT);
		logger.info("[Transfer][O][" + SessionManager.getContext(session).getRemoteAddress() + "] - Connection Established!");
	}

	@Override
	public void sessionIdle(Session session, IdleStatus status)
			throws Exception {
		logger.debug("[Transfer][I][" + SessionManager.getContext(session).getRemoteAddress() + "] - Connection IDLE:" + status + ".");
	}

	@Override
	public void sessionClosed(Session session) throws Exception {
		logger.warn("[Transfer][C][" + SessionManager.getContext(session).getRemoteAddress() + "] - Connection LOST!");		
		SessionManager.close(session);
	}
	
	@Override
	public void exceptionCaught(Session session, Throwable cause) {
		logger.error("[Transfer][E][" + SessionManager.getContext(session).getRemoteAddress() + "] - Connection ERROR:", cause);
	}

	@Override
	public void dataNotSent(Session session, Object data) throws Exception {
		logger.error("[Transfer][E][" + SessionManager.getContext(session).getRemoteAddress() + "] - Payload Not Send:" + Arrays.toString((byte[]) data) + ".");
	}

	@Override
	public void dataSent(Session session, Object data) throws Exception {
		logger.debug("[Transfer][W][" + SessionManager.getContext(session).getRemoteAddress() + "] - Payload Sent:" + Arrays.toString((byte[]) data) + ".");
	}
	
}