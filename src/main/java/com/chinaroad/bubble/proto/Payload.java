package com.chinaroad.bubble.proto;

import java.io.UTFDataFormatException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.chinaroad.foundation.utils.ArrayUtils;
import com.chinaroad.foundation.utils.ByteUtils;
import com.chinaroad.foundation.utils.CharsetUtils;

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

	public Payload put(byte[] bts) {
		this.raw = ByteUtils.merge(raw, bts);
		return this;
	}

	public Payload put(long num) {
		byte[] bts = ByteUtils.toBytes(num);
		if (Protocol.BYTE_ORDER == ByteOrder.LITTLE_ENDIAN) ArrayUtils.reverse(bts);
		this.raw = ByteUtils.merge(raw, bts);
		return this;
	}

	public Payload putUTF(String str) throws UTFDataFormatException {
		if (str == null) return this;
		this.raw = ByteUtils.merge(raw, ByteUtils.asUTF(str));
		return this;
	}

	public Payload slientPutUTF(String str) {
		try {
			this.putUTF(str);
		} catch (UTFDataFormatException cause) {
			/* Never Happen! */
		}
		return this;
	}

	public byte[] asBytes() {
		return raw;
	}
	
	public String asString() {
		return new String(raw);
	}
	
	public ByteBuffer asBuffer() {
		return ByteBuffer.wrap(raw).order(Protocol.BYTE_ORDER);
	}
	
	public String asString(String charset) {
		return this.asString(CharsetUtils.toCharset(charset));
	}
	
	public String asString(Charset charset) {
		return new String(raw, charset);
	}
	
}
