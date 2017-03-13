package com.chinaroad.bubble.biz;

import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaroad.bubble.context.BubbleManager;
import com.chinaroad.bubble.context.SessionContext;
import com.chinaroad.bubble.context.SessionManager;
import com.chinaroad.bubble.proto.Payload;
import com.chinaroad.bubble.proto.ProtoBuilder;
import com.chinaroad.bubble.proto.Protocol;
import com.chinaroad.foundation.transfer.session.Session;
import com.chinaroad.foundation.utils.ByteUtils;

public class RpcBiz {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	public Protocol.RPC_REQ request(Protocol protocol, Session session, StringBuilder _target, StringBuilder _msgid) {
		ByteBuffer buffer = protocol.getPayload().asBuffer();
		byte flags = buffer.get();
		String from = SessionManager.getContext(session).getIdentifier();
		String target = null, msgid = null;
		try {
			target = ByteUtils.readUTF(buffer);
			_target.append(target);
			msgid = ByteUtils.readUTF(buffer);
			_msgid.append(msgid);
		} catch (UTFDataFormatException e) {
			return Protocol.RPC_REQ.UNACCEPTABLE_PROTOCOL;
		}
		// Auto Select RPC Client...
		Session client = SessionContext.isIdentifier(target) ? BubbleManager.getByIdentifier(target) : BubbleManager.selectClient(target);
		if (client == null) {
			// Return Refused.
			logger.warn("[Bubble][C][" + SessionManager.getContext(session).getRemoteAddress() + "] - RPC:Request msgid(" + msgid + ") From " + from + " Refused, No Target!");
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.RPC_REQ).addPayload(Payload.create()
							.put(Protocol.RPC_REQ.REFUSED.val())
							.slientPutUTF(target)
							.slientPutUTF(msgid));
			session.send(builder.build());
			return Protocol.RPC_REQ.REFUSED;
		}
		// Do The Work!
		String to = SessionManager.getContext(client).getIdentifier();
		try {
			logger.info("[Bubble][C][" + SessionManager.getContext(session).getRemoteAddress() + "] - RPC:Request msgid(" + msgid + ") " + from + " => " + to + "...");
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.PUSH)
				.addPayload(Payload.create().put(Protocol.PushMode.RPC_REQ.val()).slientPutUTF(from))
				.addPayload(protocol.getPayload());

			client.send(builder.build());
			if ((flags & 0x80) >> 7 == 1) {	// Require Response?
				logger.info("[Bubble][C][" + SessionManager.getContext(session).getRemoteAddress() + "] - RPC:Request msgid(" + msgid + ") " + from + " => " + to + " Successfully, Watching for Response...");
				return Protocol.RPC_REQ.ACCEPTED;
			} else {	// Request Successfully!
				logger.info("[Bubble][C][" + SessionManager.getContext(session).getRemoteAddress() + "] - RPC:Request msgid(" + msgid + ") " + from + " => " + to + " Successfully!");
				builder = ProtoBuilder.create(Protocol.Type.RPC_REQ).addPayload(Payload.create()
						.put(Protocol.RPC_REQ.ACCEPTED.val())
						.slientPutUTF(target)
						.slientPutUTF(msgid));
				session.send(builder.build());
				return Protocol.RPC_REQ.ACCEPTED;
			}
		} catch (Exception e) {
			logger.info("[Bubble][C][" + SessionManager.getContext(session).getRemoteAddress() + "] - RPC:Request msgid(" + msgid + ") " + from + " => " + to + " Error!", e);
			// Return Refused.
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.RPC_REQ).addPayload(Payload.create()
							.put(Protocol.RPC_REQ.REFUSED.val())
							.slientPutUTF(target)
							.slientPutUTF(msgid));
			session.send(builder.build());
			return Protocol.RPC_REQ.REFUSED;
		}
	}

	public Protocol.RPC_RESP response(Protocol protocol, Session session, StringBuilder _target, StringBuilder _msgid) {
		ByteBuffer buffer = protocol.getPayload().asBuffer();
		@SuppressWarnings("unused")
		byte flags = buffer.get();
		String from = SessionManager.getContext(session).getIdentifier();
		String target = null, msgid = null;
		try {
			target = ByteUtils.readUTF(buffer);
			_target.append(target);
			msgid = ByteUtils.readUTF(buffer);
			_msgid.append(msgid);
		} catch (UTFDataFormatException e) {
			return Protocol.RPC_RESP.UNACCEPTABLE_PROTOCOL;
		}
		// Auto Select RPC Client...
		Session client = SessionContext.isIdentifier(target) ? BubbleManager.getByIdentifier(target) : BubbleManager.selectClient(target);
		if (client == null) {
			// Return Refused.
			logger.warn("[Bubble][C][" + SessionManager.getContext(session).getRemoteAddress() + "] - RPC:Response msgid(" + msgid + ") From " + from + " Refused, No Target!");
			return Protocol.RPC_RESP.REFUSED;
		}
		// Do The Work!
		String to = SessionManager.getContext(client).getIdentifier();
		try {
			logger.info("[Bubble][C][" + SessionManager.getContext(session).getRemoteAddress() + "] - RPC:Response msgid(" + msgid + ") " + from + " => " + to + "...");
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.PUSH)
				.addPayload(Payload.create().put(Protocol.PushMode.RPC_RESP.val()).slientPutUTF(from))
				.addPayload(protocol.getPayload());

			client.send(builder.build());
			// Response Successfully!
			logger.info("[Bubble][C][" + SessionManager.getContext(session).getRemoteAddress() + "] - RPC:Response msgid(" + msgid + ") " + from + " => " + to + " Successfully!");
			return Protocol.RPC_RESP.ACCEPTED;
		} catch (Exception e) {
			logger.info("[Bubble][C][" + SessionManager.getContext(session).getRemoteAddress() + "] - RPC:Response msgid(" + msgid + ") " + from + " => " + to + " Error!", e);
			// Return Refused.
			return Protocol.RPC_RESP.REFUSED;
		}
	}

}
