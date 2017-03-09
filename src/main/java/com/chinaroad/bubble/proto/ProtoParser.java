package com.chinaroad.bubble.proto;

import java.nio.ByteBuffer;

import com.chinaroad.bubble.proto.Protocol.Type;

public class ProtoParser {

	/**
	 * 
	 * @param bytes
	 * @return
	 * 
	 * @throws IllegalStateException for current buffer is not enough for detect payload length!
	 * @throws IllegalArgumentException for unkown protocol type.
	 */
	public static ProtoParser from(byte[] bytes) {
		return new ProtoParser(bytes);
	}
	
	private Protocol _instance = null;
	private int offset = -1;
	
	private ProtoParser(byte[] bytes) {
		if (bytes == null || bytes.length < 2) {
			throw new IllegalStateException("Current buffer is not ready from parse!");
		}
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		byte header = buffer.get();
		Type type = Type.valueOf((header & 0xF0) >> 4);
		
		int length = ProtoParser.bytesAsLength(buffer);
		if (length > 0 && (buffer.limit() - buffer.position()) < length) {
			throw new IllegalStateException("Current buffer is not complete for payload!");
		}
		
		_instance = new Protocol(type);
		if (length > 0) {
			byte[] payload = new byte[length];
			buffer.get(payload);
			_instance.addPayload(Payload.from(payload));
		} else {
			_instance.addPayload(Payload.EMPTY);
		}
		offset = buffer.position();
	}
	
	public Protocol result() {
		return _instance;
	}
	
	public int offset() {
		return offset;
	}

	private static int bytesAsLength(ByteBuffer buffer) {
		int length = 0;
		int multiplier = 1;
		byte digit;

		do {
			if (!buffer.hasRemaining()) {
				throw new IllegalStateException("Current buffer is not enough for detect payload length!");
			}
			digit = buffer.get();
			length += (digit & 0x7f) * multiplier;
			multiplier *= 128;
		} while ((digit & 0x80) > 0);
		
		return length;
	}
	
}
