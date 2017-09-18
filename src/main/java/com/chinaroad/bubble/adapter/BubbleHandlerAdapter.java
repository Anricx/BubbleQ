package com.chinaroad.bubble.adapter;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import com.chinaroad.bubble.proto.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaroad.bubble.context.SessionManager;
import com.chinaroad.foundation.transfer.handler.HandlerAdapter;
import com.chinaroad.foundation.transfer.session.IdleStatus;
import com.chinaroad.foundation.transfer.session.Session;

public abstract class BubbleHandlerAdapter extends HandlerAdapter {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private ExecutorService executorService;
	private BubbleHandlerAdapter handler = this;

	public BubbleHandlerAdapter() {
		this.executorService = Executors.newCachedThreadPool();
	}

	public BubbleHandlerAdapter(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public void sessionOpened(Session session) throws Exception {
		SessionManager.initialize(session);
		// session.setIdleTime(IdleStatus.READ_IDLE, AUTH_TIMEOUT);
		logger.info("[Transfer][O][" + SessionManager.getContext(session).getRemoteAddress() + "] - Connection Established!");
	}

	@Override
	public void sessionIdle(Session session, IdleStatus status)
			throws Exception {
		logger.error("[Transfer][I][" + SessionManager.getContext(session).getRemoteAddress() + "] - Connection IDLE:" + status + ".");
		session.close();
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

	@Override
	public void dataReceived(Session session, Object data) throws Exception {
		Protocol protocol = (Protocol) data;
		try {
			logger.debug("Prepare Thread Protocol(" + protocol + ")...");
			/**
			 * Executes the given task sometime in the future.
			 * The task may execute in a new thread or in an existing pooled thread.
			 * If the task cannot be submitted for execution,
			 * either because this executor has been shutdown or because its capacity has been reached,
			 * the task is handled by the current RejectedExecutionHandler.
			 */
			executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.debug("Executing Protocol(" + protocol + ")...");
                        handler.handle(session, protocol);
                        logger.debug("Executing Protocol(" + protocol + ") done!");
                    } catch (Exception cause) {
                        logger.error("Executing Protocol(" + protocol + ") failed...");
                        handler.exceptionCaught(session, cause);
                    }
                }
            });
		} catch (RejectedExecutionException cause) {
			logger.error("Prepare Thread Protocol(" + protocol + ") refuse!", cause);
			handler.exceptionCaught(session, cause);
		} catch (Exception cause) {
			logger.error("Prepare Thread Protocol(" + protocol + ") failed!", cause);
			handler.exceptionCaught(session, cause);
		}
	}

	public abstract void handle(Session session, Protocol protocol) throws Exception;

}