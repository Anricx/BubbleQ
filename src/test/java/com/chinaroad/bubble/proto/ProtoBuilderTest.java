package com.chinaroad.bubble.proto;

import org.junit.Assert;
import org.junit.Test;

import com.chinaroad.foundation.utils.CharsetUtils;

public class ProtoBuilderTest {

	@Test
	public void testHello() {
		String payload = "\\ua9 2017 ChinaRoad Co., Ltd.  All Rights Reserved.";
		byte[] hello = ProtoBuilder.create(Protocol.Type.HELLO).addPayload(Payload.from(payload)).build();
		ProtoParser parser = ProtoParser.from(hello);
		Protocol result = parser.result();
		
		Assert.assertEquals(Protocol.Type.HELLO, result.getType());
		Assert.assertEquals(payload, result.getPayload().asString(CharsetUtils.UTF_8));
		Assert.assertEquals(hello.length, parser.offset());
	}
	
	@Test
	public void testRpc() {
		String payload = "\\ua9 2017 ChinaRoad Co., Ltd.  All Rights Reserved.";
		byte[] request = ProtoBuilder.create(Protocol.Type.RPC_REQ).addPayload(Payload.from(payload)).build();
		ProtoParser parser = ProtoParser.from(request);
		Protocol result = parser.result();
		
		Assert.assertEquals(Protocol.Type.RPC_REQ, result.getType());
		Assert.assertEquals(payload, result.getPayload().asString(CharsetUtils.UTF_8));
		Assert.assertEquals(request.length, parser.offset());
	}

}
