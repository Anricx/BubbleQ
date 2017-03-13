package com.chinaroad.bubble.proto;

import org.junit.Assert;
import org.junit.Test;

import com.chinaroad.foundation.utils.CharsetUtils;

public class ProtoParserTest {

	@Test
	public void test() {
		String payload = "\\ua9 2015 ChinaRoad Co., Ltd.  All Rights Reserved.";
		byte[] hello = ProtoBuilder.create(Protocol.Type.HELLO).addPayload(Payload.from(payload)).build();
		ProtoParser parser = ProtoParser.from(hello);
		Protocol result = parser.result();
		
		Assert.assertEquals(Protocol.Type.HELLO, result.getType());
		Assert.assertEquals(payload, result.getPayload().asString(CharsetUtils.UTF_8));
		Assert.assertEquals(hello.length, parser.offset());
	}

	@Test
	public void test2() {
		String payload = "\\ua9 2015 ChinaRoad Co., Ltd.  All Rights Reserved.";
		byte[] hello = ProtoBuilder.create(Protocol.Type.HELLO).addPayload(Payload.from(payload)).build();
		ProtoParser parser = ProtoParser.from(hello);
		Protocol result = parser.result();
		
		Assert.assertEquals(Protocol.Type.HELLO, result.getType());
		Assert.assertEquals(payload, result.getPayload().asString(CharsetUtils.UTF_8));
		Assert.assertEquals(hello.length, parser.offset());
	}

}
