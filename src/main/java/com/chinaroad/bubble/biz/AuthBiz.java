package com.chinaroad.bubble.biz;

import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaroad.bubble.context.SessionManager;
import com.chinaroad.bubble.proto.Payload;
import com.chinaroad.bubble.proto.ProtoBuilder;
import com.chinaroad.bubble.proto.Protocol;
import com.chinaroad.foundation.transfer.session.Session;
import com.chinaroad.foundation.utils.ByteUtils;

public class AuthBiz {
	
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
	 * 
	 * @param protocol
	 * @param session
	 * @return
	 */
	public Protocol.Hello hello(Protocol protocol, Session session, final StringBuilder _name,
			final StringBuilder _identifier) {
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

		if (Protocol.VISITOR_HELLO_NAME.equals(name) || name.equals(pass)) { // Visitor
																				// Connection.
			try {
				// name@ip:port#idx
				String identifier = SessionManager.register(session, name);
				_identifier.append(identifier);
				Payload payload = Payload.create().put(Protocol.Hello.ACCEPTED.val()).slientPutUTF(identifier);
				// Feedback HELLO Response.
				ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.HELLO).addPayload(payload);
				session.send(builder.build());
				return Protocol.Hello.ACCEPTED;
			} catch (IllegalArgumentException e) {
				Payload payload = Payload.create().put(Protocol.Hello.IDENTIFIER_REJECTED.val());
				ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.HELLO).addPayload(payload);
				session.send(builder.build());
				return Protocol.Hello.IDENTIFIER_REJECTED;
			} catch (Exception e) {
				Payload payload = Payload.create().put(Protocol.Hello.SERVER_UNAVAILABLE.val());
				ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.HELLO).addPayload(payload);
				session.send(builder.build());
				return Protocol.Hello.SERVER_UNAVAILABLE;
			}
		} else {
			Payload payload = Payload.create().put(Protocol.Hello.BAD_CERTIFICATES.val());
			ProtoBuilder builder = ProtoBuilder.create(Protocol.Type.HELLO).addPayload(payload);
			session.send(builder.build());
			return Protocol.Hello.BAD_CERTIFICATES;
		}
	}
}
