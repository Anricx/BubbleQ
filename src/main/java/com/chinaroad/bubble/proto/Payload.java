package com.chinaroad.bubble.proto;

import java.io.UTFDataFormatException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.chinaroad.foundation.utils.ByteUtils;

public class Payload {

	public static final Payload EMPTY = Payload.from(Protocol.EMPTY_BYTES);
	
	public static Payload create() {
		return Payload.from(Protocol.EMPTY_BYTES);
	}
	
	public static Payload from(String payload) {
		return Payload.from(payload.getBytes());
	}

	public static Payload from(String payload, String charset) {
		try {
			return Payload.from(payload.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Payload from(byte[] bytes) {
		return new Payload().setRaw(bytes);
	}
	
	private byte[] raw;
	
	private Payload setRaw(byte[] raw) {
		this.raw = raw;
		return this;
	}

	public Payload put(byte b) {
		this.raw = ByteUtils.merge(raw, b);
		return this;
	}

	public Payload putUTF(String str) throws UTFDataFormatException {
		if (str == null) return this;
		this.raw = ByteUtils.merge(raw, ByteUtils.asUTF(str));
		return this;
	}

	public byte[] asBytes() {
		return raw;
	}
	
	public String asString() {
		return new String(raw);
	}
	
	public ByteBuffer asBuffer() {
		return ByteBuffer.wrap(raw);
	}
	
	public String asString(String charset) {
		try {
			return new String(raw, charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
