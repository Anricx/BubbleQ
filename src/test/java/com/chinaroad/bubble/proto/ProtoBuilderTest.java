package com.chinaroad.bubble.proto;

import org.junit.Assert;
import org.junit.Test;

public class ProtoBuilderTest {

	@Test
	public void test() {
		String payload = "© 2015 ChinaRoad Co., Ltd.  All Rights Reserved.";
		byte[] hello = ProtoBuilder.create(Protocol.Type.HELLO).addPayload(Payload.from(payload)).build();
		ProtoParser parser = ProtoParser.from(hello);
		Protocol result = parser.result();
		
		Assert.assertEquals(Protocol.Type.HELLO, result.getType());
		Assert.assertEquals(payload, result.getPayload().asString());
		Assert.assertEquals(hello.length, parser.offset());
	}

}
