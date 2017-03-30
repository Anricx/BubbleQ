package com.chinaroad.bubble.proto;

import java.nio.ByteOrder;

import org.junit.Assert;
import org.junit.Test;

public class ProtoBuilderTest {

	@Test
	public void testHello() {
		String payload = "\\ua9 2015 ChinaRoad Co., Ltd.  All Rights Reserved.";
		byte[] hello = ProtoBuilder.create(Protocol.Type.HELLO).addPayload(Payload.from(payload)).build();
		ProtoParser parser = ProtoParser.from(hello);
		Protocol result = parser.result();
		
		Assert.assertEquals(Protocol.Type.HELLO, result.getType());
		Assert.assertEquals(payload, result.getPayload().asString());
		Assert.assertEquals(hello.length, parser.offset());
	}
	
	@Test
	public void testRpc2() {
		long timestamp = System.currentTimeMillis();
		Payload payload = Payload.create().put(timestamp);
		
		byte[] request = ProtoBuilder.create(Protocol.Type.PING).addPayload(payload).build();
		ProtoParser parser = ProtoParser.from(request);
		Protocol result = parser.result();
		
		System.out.println(timestamp);
		System.out.println(result.getPayload().asBuffer().order(ByteOrder.LITTLE_ENDIAN).getLong());
	}
	
	@Test
	public void testRpc() {
		String payload = "\\ua9 2015 ChinaRoad Co., Ltd.  All Rights Reserved.";
		byte[] request = ProtoBuilder.create(Protocol.Type.RPC_REQ).addPayload(Payload.from(payload)).build();
		ProtoParser parser = ProtoParser.from(request);
		Protocol result = parser.result();
		
		Assert.assertEquals(Protocol.Type.RPC_REQ, result.getType());
		Assert.assertEquals(payload, result.getPayload().asString());
		Assert.assertEquals(request.length, parser.offset());
	}

}
