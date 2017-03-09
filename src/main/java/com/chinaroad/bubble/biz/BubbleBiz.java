package com.chinaroad.bubble.biz;

import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaroad.bubble.Application;
import com.chinaroad.bubble.adapter.SessionAdapter;
import com.chinaroad.bubble.bean.Locker;
import com.chinaroad.bubble.proto.Payload;
import com.chinaroad.bubble.proto.ProtoBuilder;
import com.chinaroad.bubble.proto.Protocol;
import com.chinaroad.bubble.proto.Protocol.Publish;
import com.chinaroad.foundation.transfer.session.Session;
import com.chinaroad.foundation.utils.ByteUtils;

public class BubbleBiz {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * <pre>
	 * <h1>Connect Return code values</h1>
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------
	 * | <b>Value</b> | <b>Return Code Response</b>                                   | <b>Description</b>                                                                           |
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------
	 * |   0   | 0x00 Connection Accepted                               | Connection accepted                                                                   |
	 * |   1   | 0x01 Connection Refused, unacceptable protocol version | The Server does not support the level of the BubbleQ protocol requested by the Client |
	 * |   2   | 0x02 Connection Refused, identifier rejected           | The Client identifier is correct but not allowed by the Server                        |
	 * |   3   | 0x03 Connection Refused, Server unavailable            | The Network Connection has been made but the BubbleQ service is unavailable           |
	 * |   4   | 0x04 Connection Refused, bad user name or password     | The data in the user name or password is malformed                                    |
	 * |   5   | 0x05 Connection Refused, not authorized                | The Client is not authorized to connect                                               |
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------
	 * </pre>
	 * @param protocol
	 * @param session
	 * @return
	 */
	public Protocol.Hello hello(Protocol protocol, Session session, final StringBuffer _identifier) {
		ByteBuffer buffer = protocol.getPayload().asBuffer();
		String name = null, pass = null;
		try {
			name = ByteUtils.readUTF(buffer);
			if (!Protocol.VISITOR_HELLO_NAME.equals(name)) {
				pass = ByteUtils.readUTF(buffer);
			}
		} catch (UTFDataFormatException e) {
			Payload payload = Payload.create().put(Protocol.Hello.UNACCEPTABLE_PROTOCOL.val());
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.HELLO).addPayload(payload);
			session.send(builder.build());
			return Protocol.Hello.UNACCEPTABLE_PROTOCOL;
		}
		
		if (Protocol.VISITOR_HELLO_NAME.equals(name) || name.equals(pass)) {	// Visitor Connection.
			// name@ip:port#idx
			String identifier = SessionAdapter.generateIdentifier(session, name);
			_identifier.append(identifier);
			try {
				Payload payload = Payload.create().put(Protocol.Hello.ACCEPTED.val()).putUTF(identifier);
				// Feedback HELLO Response.
				ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.HELLO).addPayload(payload);
				session.send(builder.build());
				return Protocol.Hello.ACCEPTED;
			} catch (UTFDataFormatException e) {
				SessionAdapter.releaseIdentifier(session);
				Payload payload = Payload.create().put(Protocol.Hello.UNACCEPTABLE_PROTOCOL.val());
				ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.HELLO).addPayload(payload);
				session.send(builder.build());
				return Protocol.Hello.UNACCEPTABLE_PROTOCOL;
			}
		} else {
			Payload payload = Payload.create().put(Protocol.Hello.BAD_CERTIFICATES.val());
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.HELLO).addPayload(payload);
			session.send(builder.build());
			return Protocol.Hello.BAD_CERTIFICATES;
		}
	}

	public Protocol.Subscribe subscribe(Protocol protocol, Session session, final StringBuffer _topic) {
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
			synchronized (Locker.FOR_TOPIC) {
				if (!Application.TOPICS.containsKey(topic)) {
					Application.TOPICS.put(topic, new LinkedList<Session>());
				}
				if (!Application.TOPICS.get(topic).contains(session)) {
					Application.TOPICS.get(topic).offer(session);	// Let Global Server Know!
					SessionAdapter.getSessionContext(session).addTopic(topic);	// Let Current Session Know!
				}
			}
			
			Payload payload = Payload.create().put(Protocol.Subscribe.ACCEPTED.val()).putUTF(topic);
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.SUBSCRIBE).addPayload(payload);
			session.send(builder.build());
			
			return Protocol.Subscribe.ACCEPTED;
		} catch (UTFDataFormatException e) {
			Payload payload = Payload.create().put(Protocol.Subscribe.REFUSED.val());
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.SUBSCRIBE).addPayload(payload);
			session.send(builder.build());
			return Protocol.Subscribe.REFUSED;
		}
	}

	public Protocol.Listen listen(Protocol protocol, Session session, final StringBuffer _topic) {
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
			synchronized (Locker.FOR_LISTENER) {
				if (!Application.LISTENERS.containsKey(topic)) {
					Application.LISTENERS.put(topic, new LinkedList<Session>());
				}
				if (!Application.LISTENERS.get(topic).contains(session)) {
					Application.LISTENERS.get(topic).offer(session);	// Let Global Server Know!
					SessionAdapter.getSessionContext(session).addListening(topic);	// Let Current Session Know!
				}
			}
			
			Payload payload = Payload.create().put(Protocol.Listen.ACCEPTED.val()).putUTF(topic);
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.LISTEN).addPayload(payload);
			session.send(builder.build());
			
			return Protocol.Listen.ACCEPTED;
		} catch (UTFDataFormatException e) {
			Payload payload = Payload.create().put(Protocol.Listen.REFUSED.val());
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.LISTEN).addPayload(payload);
			session.send(builder.build());
			return Protocol.Listen.REFUSED;
		}
	}

	public Publish publish(Protocol protocol, Session session, final StringBuffer _topic, final StringBuffer _msgid) {
		ByteBuffer buffer = protocol.getPayload().asBuffer();
		byte flags = buffer.get();
		boolean broadcast = (flags & 0x80) >> 7 == 1;
		String from = SessionAdapter.getIdentifier(session);
		String topic = null, msgid = null;
		try {
			topic = ByteUtils.readUTF(buffer);
			msgid = ByteUtils.readUTF(buffer);
			_topic.append(topic);
			_msgid.append(msgid);
		} catch (UTFDataFormatException e) {
			return Protocol.Publish.UNACCEPTABLE_PROTOCOL;
		}
		if (!Application.TOPICS.containsKey(topic)) {
			// Return Refused.
			try {
				Payload payload = Payload.create().put(Protocol.Publish.REFUSED.val()).putUTF(topic).putUTF(msgid);
				ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.PUBLISH).addPayload(payload);
				session.send(builder.build());
			} catch (UTFDataFormatException cause) {
				/* Never Happen! */
			}
			return Protocol.Publish.REFUSED;
		}
		// # Do the job.
		try {
			if (broadcast) {
				/* Do Broadcast... */
				// Return Accepted.
				ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.PUBLISH)
						.addPayload(Payload.create().put(Protocol.Publish.ACCEPTED.val()).putUTF(topic).putUTF(msgid));
				session.send(builder.build());
				LinkedList<Session> targets = Application.TOPICS.get(topic);

				logger.info("[Bubble][P][" + SessionAdapter.clientAddress(session) + "] - Try to Broadcast Topic:\"" + topic + "\", msgid(" + msgid + ") From \"" + from + "\" To \"" + targets + "\"...");
				for (Iterator<Session> iterator = targets.iterator(); iterator.hasNext();) {
					Session target = iterator.next();
					builder = ProtoBuilder.create(Protocol.Type.PUSH)
							.addPayload(Payload.create().put(Protocol.PushMode.PUBLISH.val()).putUTF(from))
							.addPayload(protocol.getPayload());
					try {
						target.send(builder.build());
						logger.info("[Bubble][P][" + SessionAdapter.clientAddress(session) + "] - Broadcast Topic:\"" + topic + "\", msgid(" + msgid + ") From \"" + from + "\" To \"" + target + "\" Successfully!");
					} catch (IllegalStateException e) {
						logger.error("[Bubble][P][" + SessionAdapter.clientAddress(session) + "] - Broadcast Topic:\"" + topic + "\", msgid(" + msgid + ") From \"" + from + "\" To \"" + target + "\" Error! Connection Lost?", e);
						iterator.remove();
					}
				}
				// Push Payload To Listeners...
				if (Application.LISTENERS.containsKey(topic)) {
					LinkedList<Session> listeners = Application.LISTENERS.get(topic);
					for (Iterator<Session> iterator = listeners.iterator(); iterator.hasNext();) {
						Session listener = iterator.next();
						builder = ProtoBuilder.create(Protocol.Type.PUSH)
								.addPayload(Payload.create().put(Protocol.PushMode.LISTEN.val()).putUTF(from))
								.addPayload(protocol.getPayload());
						try {
							listener.send(builder.build());
							logger.info("[Bubble][*][" + SessionAdapter.clientAddress(session) + "] - Whisper Topic:\"" + topic + "\", msgid(" + msgid + ") From \"" + from + "\" To \"" + listener + "\" Successfully!");
						} catch (IllegalStateException e) {
							logger.error("[Bubble][*][" + SessionAdapter.clientAddress(session) + "] - Whisper Topic:\"" + topic + "\", msgid(" + msgid + ") From \"" + from + "\" To \"" + listener + "\" Error! Connection Lost?", e);
							iterator.remove();
						}
					}
				}
				
				logger.info("[Bubble][P][" + SessionAdapter.clientAddress(session) + "] - Broadcast Topic:\"" + topic + "\", msgid(" + msgid + ") From \"" + from + "\" To \"" + targets + "\" Finished!");
				return Protocol.Publish.ACCEPTED;
			} else {
				/* Lock our target Bubble */
				Session target = null;
				if (Application.TOPICS.get(topic).size() > 1) {
					target = Application.TOPICS.get(topic).poll();	// %Loop First To Publish
					Application.TOPICS.get(topic).offer(target);	// %Add To Last
				} else if (Application.TOPICS.get(topic).size() == 1) {
					target = Application.TOPICS.get(topic).peek();
				} else {
					// Return Refused.
					try {
						Payload payload = Payload.create().put(Protocol.Publish.REFUSED.val()).putUTF(topic).putUTF(msgid);
						ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.PUBLISH).addPayload(payload);
						session.send(builder.build());
					} catch (UTFDataFormatException cause) {
						/* Never Happen! */
					}
					return Protocol.Publish.REFUSED;
				}
				String to = SessionAdapter.getIdentifier(target);
				
				// Return Accepted.
				ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.PUBLISH)
						.addPayload(Payload.create().put(Protocol.Publish.ACCEPTED.val()).putUTF(topic).putUTF(msgid));
				session.send(builder.build());
				
				logger.info("[Bubble][P][" + SessionAdapter.clientAddress(session) + "] - Try to Push Topic:\"" + topic + "\", msgid(" + msgid + ") From \"" + from + "\" To \"" + to + "\"...");
				builder = ProtoBuilder.create(Protocol.Type.PUSH)
						.addPayload(Payload.create().put(Protocol.PushMode.PUBLISH.val()).putUTF(from))
						.addPayload(protocol.getPayload());
				target.send(builder.build());

				// Push Payload To Listeners...
				if (Application.LISTENERS.containsKey(topic)) {
					LinkedList<Session> listeners = Application.LISTENERS.get(topic);
					for (Iterator<Session> iterator = listeners.iterator(); iterator.hasNext();) {
						Session listener = (Session) iterator.next();
						builder = ProtoBuilder.create(Protocol.Type.PUSH)
								.addPayload(Payload.create().put(Protocol.PushMode.LISTEN.val()).putUTF(from))
								.addPayload(protocol.getPayload());
						try {
							listener.send(builder.build());
							logger.info("[Bubble][*][" + SessionAdapter.clientAddress(session) + "] - Whisper Topic:\"" + topic + "\", msgid(" + msgid + ") From \"" + from + "\" To \"" + listener + "\" Successfully!");
						} catch (IllegalStateException e) {
							logger.error("[Bubble][*][" + SessionAdapter.clientAddress(session) + "] - Whisper Topic:\"" + topic + "\", msgid(" + msgid + ") From \"" + from + "\" To \"" + listener + "\" Error! Connection Lost?", e);
							iterator.remove();
						}
					}
				}
				
				logger.info("[Bubble][P][" + SessionAdapter.clientAddress(session) + "] - Push Topic:\"" + topic + "\", msgid(" + msgid + ") From \"" + from + "\" To \"" + to + "\" Finished!");
				return Protocol.Publish.ACCEPTED;
			}
		} catch (UTFDataFormatException e) {
			try {
				Payload payload = Payload.create().put(Protocol.Publish.REFUSED.val()).putUTF(topic).putUTF(msgid);
				ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.PUBLISH).addPayload(payload);
				session.send(builder.build());
			} catch (UTFDataFormatException cause) {
				/* Never Happen! */
			}
			return Protocol.Publish.REFUSED;
		}
	}

	public Protocol.Feedback feedback(Protocol protocol, Session session, final StringBuffer _target, final StringBuffer _topic, final StringBuffer _msgid) {
		ByteBuffer buffer = protocol.getPayload().asBuffer();
		String from = SessionAdapter.getIdentifier(session);
		byte flags = buffer.get();
		String target = null, topic = null, msgid = null;
		Payload payload = null;
		try {
			target = ByteUtils.readUTF(buffer);
			topic = ByteUtils.readUTF(buffer);
			msgid = ByteUtils.readUTF(buffer);
			payload = Payload.from(ByteUtils.asBytes(buffer));

			_target.append(target);
			_topic.append(topic);
			_msgid.append(msgid);
		} catch (UTFDataFormatException e) {
			return Protocol.Feedback.UNACCEPTABLE_PROTOCOL;
		}
		if (!Application.CLIENTS.containsKey(target)) {
			return Protocol.Feedback.REFUSED;
		}
		// # Do the job.
		try {
			// Return Accepted.
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.FEEDBACK)
					.addPayload(Payload.create().put(Protocol.Feedback.ACCEPTED.val()).putUTF(target).putUTF(topic).putUTF(msgid));
			session.send(builder.build());
			
			logger.info("[Bubble][P][" + SessionAdapter.clientAddress(session) + "] - Feedback Topic:\"" + topic + "\", msgid(" + msgid + ") From \"" + from + "\" To \"" + target + "\".");
			builder = ProtoBuilder.create(Protocol.Type.PUSH)
					.addPayload(Payload.create().put(Protocol.PushMode.FEEDBACK.val()).putUTF(from))
					.addPayload(Payload.create().put(flags).putUTF(topic))
					.addPayload(Payload.create().putUTF(msgid))
					.addPayload(payload);
			Application.CLIENTS.get(target).send(builder.build());
			
			return Protocol.Feedback.ACCEPTED;
		} catch (UTFDataFormatException e) {
			try {
				ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.FEEDBACK)
						.addPayload(Payload.create().put(Protocol.Feedback.REFUSED.val()).putUTF(target).putUTF(topic).putUTF(msgid));
				session.send(builder.build());
			} catch (UTFDataFormatException cause) {
				/* Never Happen! */
			}
			return Protocol.Feedback.REFUSED;
		}
	}

}
