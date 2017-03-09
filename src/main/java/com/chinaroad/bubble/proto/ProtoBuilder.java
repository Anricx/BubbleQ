package com.chinaroad.bubble.proto;

import java.nio.ByteBuffer;

import com.chinaroad.bubble.proto.Protocol.Type;

public class ProtoBuilder {

	public static ProtoBuilder create(Type type) {
		return new ProtoBuilder(type);
	}

	private Protocol _instance = null;
	
	public ProtoBuilder(Type type) {
		_instance = new Protocol(type);
	}
	
	public ProtoBuilder addPayload(Payload payload) {
		_instance.addPayload(payload);
		return this;
	}
	
	public Protocol instance() {
		return _instance;
	}
	
	public byte[] build() {
		Type type = _instance.getHeader().getType();
		byte header = (byte) (type.val() << 4);
		byte[] payload = _instance.getPayload() == null ? Protocol.EMPTY_BYTES : _instance.getPayload().asBytes();
		byte[] length = ProtoBuilder.lengthAsBytes(payload.length);
		ByteBuffer buffer = ByteBuffer.allocate(
					1 + 	// fix header byte 1
					length.length +	// the remaining length
					payload.length	// here come the payload
				);
		
		buffer.put(header);
		buffer.put(length);
		buffer.put(payload);
		buffer.flip();
		
		return buffer.array();
	}
	
	private static byte[] lengthAsBytes(int length) {
		if (length > Protocol.REMAINING_LENGTH_LIMIT) throw new RuntimeException("Reach max length limit: " + Protocol.REMAINING_LENGTH_LIMIT + ".");
		ByteBuffer buf = ByteBuffer.allocate(4);
		int i = 0;
		do {
			byte digit = (byte) (length & 0x7F);
			length >>= 7;
			if (length > 0) {
				digit |= 0x80;
			}
			buf.put(digit);
			i ++;
		} while (length > 0);
		byte[] bts = new byte[i];
		buf.flip();
		buf.get(bts);
		return bts;
	}
}
