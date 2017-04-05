package com.chinaroad.bubble.proto;

import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import com.chinaroad.foundation.transfer.session.Session;
import com.chinaroad.foundation.transfer.session.SocketSession;
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
		
		LinkedList<Session> sessions = new LinkedList<Session>(); 
		Session a = new SocketSession();
		sessions.add(new SocketSession());
		sessions.add(a);
		sessions.add(new SocketSession());
		sessions.add(new SocketSession());
		
		System.out.println(sessions);
		sessions.remove(a);
		System.out.println(sessions);
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
