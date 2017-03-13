package com.chinaroad.bubble.proto;

import java.util.LinkedHashSet;
import java.util.Set;

import com.chinaroad.foundation.utils.ByteUtils;

/**
 * <h1>BubbleQ v1.0 Protocol Specification</h1>
 * The message header for each BubbleQ command message contains a fixed header. 
 * The table below shows the fixed header format.
 * <pre>
 * ----------------------------------------------------------
 * | bit    | 7    6    5    4 |   3    |  2    1   |0      |
 * ----------------------------------------------------------
 * | byte 1 |  Message Type    |                            |
 * | byte 2 |  Remaining Length                             |
 * ----------------------------------------------------------
 * </pre>
 * 
 * <b>Byte 1</b><br>
 * Contains the Message Type fields.<br>
 * <b>Byte 2</b><br>
 * (At least one byte) contains the Remaining Length field.
 * The fields are described in the following sections. 
 * All data values are in big-endian order: higher order bytes precede lower order bytes. 
 * 
 * @author <a href="mailto:joe.dengtao@gmail.com">D.T.</a>
 */
public class Protocol {

	public static enum PushMode {
		PUBLISH((byte) 0x00),
		LISTEN((byte) 0x10),
		RPC_REQ((byte) 0x02),
		RPC_RESP((byte) 0x03);
		
		private byte val;
		
		PushMode(byte val) {
			this.val = val;
		}
		
		public byte val() {
			return val;
		}
		
		public static PushMode valueOf(byte val) {
			for(PushMode t: PushMode.values()) {
				if (t.val == val)
					return t;
			}
			throw new IllegalArgumentException("Unkown Push Mode: " + val + "!");
		}
	}
	
	public static enum Publish {
		ACCEPTED((byte) 0x00),
		REFUSED((byte) 0x01),
		UNACCEPTABLE_PROTOCOL((byte) 0x02);
		
		private byte val;
		
		Publish(byte val) {
			this.val = val;
		}
		
		public byte val() {
			return val;
		}
		
		public static Publish valueOf(byte val) {
			for(Publish t: Publish.values()) {
				if (t.val == val)
					return t;
			}
			throw new IllegalArgumentException("Unkown Subscribe Status: " + val + "!");
		}
	}
	
	public static enum Subscribe {
		ACCEPTED((byte) 0x00),
		REFUSED((byte) 0x01),
		UNACCEPTABLE_PROTOCOL((byte) 0x02);
		
		private byte val;
		
		Subscribe(byte val) {
			this.val = val;
		}
		
		public byte val() {
			return val;
		}
		
		public static Subscribe valueOf(byte val) {
			for(Subscribe t: Subscribe.values()) {
				if (t.val == val)
					return t;
			}
			throw new IllegalArgumentException("Unkown Subscribe Status: " + val + "!");
		}
	}
	
	public static enum RPC_REQ {
		ACCEPTED((byte) 0x00),
		REFUSED((byte) 0x01),
		UNACCEPTABLE_PROTOCOL((byte) 0x02);
		
		private byte val;
		
		RPC_REQ(byte val) {
			this.val = val;
		}
		
		public byte val() {
			return val;
		}
		
		public static RPC_REQ valueOf(byte val) {
			for(RPC_REQ t: RPC_REQ.values()) {
				if (t.val == val)
					return t;
			}
			throw new IllegalArgumentException("Unkown RPC:Request Status: " + val + "!");
		}
	}
	
	public static enum RPC_RESP {
		ACCEPTED((byte) 0x00),
		REFUSED((byte) 0x01),
		UNACCEPTABLE_PROTOCOL((byte) 0x02);
		
		private byte val;
		
		RPC_RESP(byte val) {
			this.val = val;
		}
		
		public byte val() {
			return val;
		}
		
		public static RPC_RESP valueOf(byte val) {
			for(RPC_RESP t: RPC_RESP.values()) {
				if (t.val == val)
					return t;
			}
			throw new IllegalArgumentException("Unkown RPC:Response Status: " + val + "!");
		}
	}
	
	public static enum Listen {
		ACCEPTED((byte) 0x00),
		REFUSED((byte) 0x01),
		UNACCEPTABLE_PROTOCOL((byte) 0x02);
		
		private byte val;
		
		Listen(byte val) {
			this.val = val;
		}
		
		public byte val() {
			return val;
		}
		
		public static Listen valueOf(byte val) {
			for(Listen t: Listen.values()) {
				if (t.val == val)
					return t;
			}
			throw new IllegalArgumentException("Unkown Listen Status: " + val + "!");
		}
	}
	
	public static enum Hello {
		
		ACCEPTED((byte) 0x00),
		UNACCEPTABLE_PROTOCOL((byte) 0x01),
		IDENTIFIER_REJECTED((byte) 0x02),
		SERVER_UNAVAILABLE((byte) 0x03),
		BAD_CERTIFICATES((byte) 0x04),
		NOT_AUTHORIZED((byte) 0x05);
		
		private byte val;
		
		Hello(byte val) {
			this.val = val;
		}
		
		public byte val() {
			return val;
		}
		
		public static Hello valueOf(byte val) {
			for(Hello t: Hello.values()) {
				if (t.val == val)
					return t;
			}
			throw new IllegalArgumentException("Unkown Hello Status: " + val + "!");
		}
		
	}
	
	/**
	 * Represents the number of bytes remaining within the current message, including data in the variable header and the payload.
	 * The variable length encoding scheme uses a single byte for messages up to 127 bytes long. Longer messages are handled as follows. Seven bits of each byte encode the Remaining Length data, and the eighth bit indicates any following bytes in the representation. Each byte encodes 128 values and a "continuation bit". For example, the number 64 decimal is encoded as a single byte, decimal value 64, hex 0x40. The number 321 decimal (= 65 + 2*128) is encoded as two bytes, least significant first. The first byte 65+128 = 193. Note that the top bit is set to indicate at least one following byte. The second byte is 2.
	 * The protocol limits the number of bytes in the representation to a maximum of four. This allows applications to send messages of up to 268 435 455 (256 MB). The representation of this number on the wire is: 0xFF, 0xFF, 0xFF, 0x7F.
	 * The table below shows the Remaining Length values represented by increasing numbers of bytes.
     * --------------------------------------------------------------------------------------
	 * | Digits	| From	                             | To                                   |
     * --------------------------------------------------------------------------------------
	 * | 1	    | 0 (0x00)	                         | 127 (0x7F)                           |
	 * | 2	    | 128 (0x80, 0x01)	                 | 16 383 (0xFF, 0x7F)                  |
	 * | 3	    | 16 384 (0x80, 0x80, 0x01)	         | 2 097 151 (0xFF, 0xFF, 0x7F)         |
	 * | 4	    | 2 097 152 (0x80, 0x80, 0x80, 0x01) | 268 435 455 (0xFF, 0xFF, 0xFF, 0x7F) |
     * --------------------------------------------------------------------------------------
	 */
	protected static final int REMAINING_LENGTH_LIMIT = 268435455;
	
	/**
	 * if client is as a visitor, pass is not required!
	 */
	public static final String VISITOR_HELLO_NAME = "visitor";
	
	protected static final byte[] EMPTY_BYTES = new byte[0];
	
	public enum Type {
		HELLO((byte) 1),
		SUBSCRIBE((byte) 2),
		PUBLISH((byte) 3),
		PUSH((byte) 5),
		LISTEN((byte) 6),

		RPC_REQ((byte) 7),
		RPC_RESP((byte) 9),

		// UNSUBSCRIBE((byte) 14),
		BYE((byte) 15),
		PING((byte) 0);
		
		private byte val;
		
		Type(byte val) {
			this.val = val;
		}
		
		public byte val() {
			return val;
		}
		
		static Type valueOf(int val) {
			for(Type t: Type.values()) {
				if (t.val == val)
					return t;
			}
			throw new IllegalArgumentException("Unkown protocol type: " + val + "!");
		}
		
	}
	
	private Header header;
	private Set<Payload> payloads;
	
	public Protocol(Type type) {
		this.setHeader(new Header(type));
	}
	
	protected Header getHeader() {
		return header;
	}

	protected Protocol setHeader(Header header) {
		this.header = header;
		return this;
	}
	
	public Type getType() {
		return header == null ? null : header.getType();
	}

	protected Protocol addPayload(Payload payload) {
		if (payloads == null) payloads = new LinkedHashSet<Payload>();
		payloads.add(payload);
		return this;
	}
	
	public Payload getPayload() {
		if (payloads == null) return Payload.EMPTY;
		byte[] bytes = Protocol.EMPTY_BYTES;
		for (Payload payload : payloads) {
			bytes = ByteUtils.merge(bytes, payload.asBytes());
		}
		return Payload.from(bytes);
	}
	
}
