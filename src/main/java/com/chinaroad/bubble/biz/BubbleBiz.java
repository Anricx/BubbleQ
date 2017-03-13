package com.chinaroad.bubble.biz;

import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaroad.bubble.context.SessionManager;
import com.chinaroad.bubble.context.TopicManager;
import com.chinaroad.bubble.proto.Payload;
import com.chinaroad.bubble.proto.ProtoBuilder;
import com.chinaroad.bubble.proto.Protocol;
import com.chinaroad.bubble.proto.Protocol.Publish;
import com.chinaroad.foundation.transfer.session.Session;
import com.chinaroad.foundation.utils.ByteUtils;

public class BubbleBiz {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	public Protocol.Subscribe subscribe(Protocol protocol, Session session, final StringBuilder _topic) {
		ByteBuffer buffer = protocol.getPayload().asBuffer();
		String topic = null;
		try {
			topic = ByteUtils.readUTF(buffer);
			_topic.append(topic);
		} catch (UTFDataFormatException e) {
			return Protocol.Subscribe.UNACCEPTABLE_PROTOCOL;
		}
		// #Subscribe Topic
		try {
			TopicManager.subscribe(session, topic);
			Payload payload = Payload.create().put(Protocol.Subscribe.ACCEPTED.val()).slientPutUTF(topic);
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.SUBSCRIBE).addPayload(payload);
			session.send(builder.build());
			
			return Protocol.Subscribe.ACCEPTED;
		} catch (Exception e) {
			Payload payload = Payload.create().put(Protocol.Subscribe.REFUSED.val());
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.SUBSCRIBE).addPayload(payload);
			session.send(builder.build());
			return Protocol.Subscribe.REFUSED;
		}
	}

	public Protocol.Listen listen(Protocol protocol, Session session, final StringBuilder _topic) {
		ByteBuffer buffer = protocol.getPayload().asBuffer();
		String topic = null;
		try {
			topic = ByteUtils.readUTF(buffer);
			_topic.append(topic);
		} catch (UTFDataFormatException e) {
			return Protocol.Listen.UNACCEPTABLE_PROTOCOL;
		}
		// #Subscribe Topic
		try {
			TopicManager.listening(session, topic);
			Payload payload = Payload.create().put(Protocol.Listen.ACCEPTED.val()).slientPutUTF(topic);
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.LISTEN).addPayload(payload);
			session.send(builder.build());
			
			return Protocol.Listen.ACCEPTED;
		} catch (Exception e) {
			Payload payload = Payload.create().put(Protocol.Listen.REFUSED.val());
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.LISTEN).addPayload(payload);
			session.send(builder.build());
			return Protocol.Listen.REFUSED;
		}
	}

	public void ping(Protocol protocol, Session session) {
		logger.info("[Bubble][-][" + SessionManager.getContext(session).getRemoteAddress() + "] - PING...");
		ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.PING).addPayload(protocol.getPayload());
		session.send(builder.build());
	}

	public Publish publish(Protocol protocol, Session session, final StringBuilder _topic, final StringBuilder _msgid) {
		ByteBuffer buffer = protocol.getPayload().asBuffer();
		byte flags = buffer.get();
		boolean broadcast = (flags & 0x80) >> 7 == 1;
		String from = SessionManager.getContext(session).getIdentifier();
		String topic = null, msgid = null;
		try {
			topic = ByteUtils.readUTF(buffer);
			_topic.append(topic);
			msgid = ByteUtils.readUTF(buffer);
			_msgid.append(msgid);
		} catch (UTFDataFormatException e) {
			return Protocol.Publish.UNACCEPTABLE_PROTOCOL;
		}
		if (!TopicManager.hasCustomer(topic)) {
			// Return Refused.
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.PUBLISH).addPayload(Payload.create()
					.put(Protocol.Publish.REFUSED.val())
					.slientPutUTF(topic)
					.slientPutUTF(msgid));
			session.send(builder.build());
			return Protocol.Publish.REFUSED;
		}
		// # Do the job.
		try {
			if (broadcast) {
				/* Do Broadcast... */
				// Return Accepted.
				ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.PUBLISH).addPayload(Payload.create()
						.put(Protocol.Publish.ACCEPTED.val())
						.slientPutUTF(topic)
						.slientPutUTF(msgid));
				session.send(builder.build());
				LinkedList<Session> customers = TopicManager.getAllCustomers(topic);
				String[] identifiers = SessionManager.getIdentifiers(customers);
				logger.info("[Bubble][P][" + SessionManager.getContext(session).getRemoteAddress() + "] - Try to Broadcast Topic:" + topic + ", msgid(" + msgid + ") From " + from + " To " + Arrays.toString(identifiers) + "...");
				for (Iterator<Session> iterator = customers.iterator(); iterator.hasNext();) {
					Session customer = iterator.next();
					String to = SessionManager.getContext(customer).getIdentifier();
					builder = ProtoBuilder.create(Protocol.Type.PUSH)
							.addPayload(Payload.create().put(Protocol.PushMode.PUBLISH.val()).slientPutUTF(from))
							.addPayload(protocol.getPayload());
					try {
						customer.send(builder.build());
						logger.info("[Bubble][P][" + SessionManager.getContext(session).getRemoteAddress() + "] - Push Topic:" + topic + ", msgid(" + msgid + ") From " + from + " To " + to + " Successfully!");
					} catch (IllegalStateException e) {
						logger.error("[Bubble][P][" + SessionManager.getContext(session).getRemoteAddress() + "] - Push Topic:" + topic + ", msgid(" + msgid + ") From " + from + " To " + to + " Error! Connection Lost?", e);
						iterator.remove();
					}
				}
				if (!TopicManager.hasListener(topic)) {
					logger.info("[Bubble][P][" + SessionManager.getContext(session).getRemoteAddress() + "] - Broadcast Topic:" + topic + ", msgid(" + msgid + ") From " + from + " To " + Arrays.toString(identifiers) + " Successfully!");
					return Protocol.Publish.ACCEPTED;
				}
				// Push Payload To Listeners...
				LinkedList<Session> listeners = TopicManager.getAllListeners(topic);
				for (Iterator<Session> iterator = listeners.iterator(); iterator.hasNext();) {
					Session listener = iterator.next();
					String whisper = SessionManager.getContext(listener).getIdentifier();
					builder = ProtoBuilder.create(Protocol.Type.PUSH)
							.addPayload(Payload.create().put(Protocol.PushMode.LISTEN.val()).slientPutUTF(from))
							.addPayload(protocol.getPayload());
					try {
						listener.send(builder.build());
						logger.info("[Bubble][*][" + SessionManager.getContext(session).getRemoteAddress() + "] - Whisper Topic:" + topic + ", msgid(" + msgid + ") From " + from + " To " + whisper + " Successfully!");
					} catch (IllegalStateException e) {
						logger.error("[Bubble][*][" + SessionManager.getContext(session).getRemoteAddress() + "] - Whisper Topic:" + topic + ", msgid(" + msgid + ") From " + from + " To " + whisper + " Error! Connection Lost?", e);
						iterator.remove();
					}
				}
				
				logger.info("[Bubble][P][" + SessionManager.getContext(session).getRemoteAddress() + "] - Broadcast Topic:" + topic + ", msgid(" + msgid + ") From " + from + " To " + Arrays.toString(identifiers) + " Successfully!");
				return Protocol.Publish.ACCEPTED;
			} else {
				/* Lock our target Bubble */
				Session customer = TopicManager.getCustomer(topic);
				if (customer == null) {
					// Return Refused.
					ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.PUBLISH).addPayload(Payload.create()
							.put(Protocol.Publish.REFUSED.val())
							.slientPutUTF(topic)
							.slientPutUTF(msgid));
					session.send(builder.build());
					return Protocol.Publish.REFUSED;
				}
				String to = SessionManager.getContext(customer).getIdentifier();
				// Return Accepted.
				ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.PUBLISH)
						.addPayload(Payload.create().put(Protocol.Publish.ACCEPTED.val()).slientPutUTF(topic).slientPutUTF(msgid));
				session.send(builder.build());
				
				logger.info("[Bubble][P][" + SessionManager.getContext(session).getRemoteAddress() + "] - Try to Push Topic:" + topic + ", msgid(" + msgid + ") From " + from + " To " + to + "...");
				builder = ProtoBuilder.create(Protocol.Type.PUSH)
						.addPayload(Payload.create().put(Protocol.PushMode.PUBLISH.val()).slientPutUTF(from))
						.addPayload(protocol.getPayload());
				customer.send(builder.build());

				if (!TopicManager.hasListener(topic)) {
					logger.info("[Bubble][P][" + SessionManager.getContext(session).getRemoteAddress() + "] - Push Topic:" + topic + ", msgid(" + msgid + ") From " + from + " To " + to + " Successfully!");
					return Protocol.Publish.ACCEPTED;
				}
				
				// Push Payload To Listeners...
				LinkedList<Session> listeners = TopicManager.getAllListeners(topic);
				for (Iterator<Session> iterator = listeners.iterator(); iterator.hasNext();) {
					Session listener = (Session) iterator.next();
					String whisper = SessionManager.getContext(listener).getIdentifier();
					builder = ProtoBuilder.create(Protocol.Type.PUSH)
							.addPayload(Payload.create().put(Protocol.PushMode.LISTEN.val()).slientPutUTF(from))
							.addPayload(protocol.getPayload());
					try {
						listener.send(builder.build());
						logger.info("[Bubble][*][" + SessionManager.getContext(session).getRemoteAddress() + "] - Whisper Topic:" + topic + ", msgid(" + msgid + ") From " + from + " To " + whisper + " Successfully!");
					} catch (IllegalStateException e) {
						logger.error("[Bubble][*][" + SessionManager.getContext(session).getRemoteAddress() + "] - Whisper Topic:" + topic + ", msgid(" + msgid + ") From " + from + " To " + whisper + " Error! Connection Lost?", e);
						iterator.remove();
					}
				}
				logger.info("[Bubble][P][" + SessionManager.getContext(session).getRemoteAddress() + "] - Push Topic:" + topic + ", msgid(" + msgid + ") From " + from + " To " + to + " Successfully!");
				return Protocol.Publish.ACCEPTED;
			}
		} catch (Exception e) {
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.PUBLISH).addPayload(Payload.create()
					.put(Protocol.Publish.REFUSED.val())
					.slientPutUTF(topic)
					.slientPutUTF(msgid));
			session.send(builder.build());
			return Protocol.Publish.REFUSED;
		}
	}

}
