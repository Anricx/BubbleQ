package com.chinaroad.bubble.filter;

import java.util.HashMap;
import java.util.Map;

import com.chinaroad.bubble.proto.ProtoParser;
import com.chinaroad.foundation.transfer.filter.FilterAdapter;
import com.chinaroad.foundation.transfer.filter.FilterEntity;
import com.chinaroad.foundation.transfer.session.Session;
import com.chinaroad.foundation.utils.ByteUtils;

public class ProtoFilter extends FilterAdapter {

	private Map<Session, byte[]> buffers = new HashMap<Session, byte[]>();

	@Override
	public void dataReceived(FilterEntity nextEntity, Session session,
			Object data) throws Exception {
		// #1: merge buffer
		byte[] buffer = ByteUtils.EMPTY;
		if (buffers.containsKey(session)) buffer = buffers.remove(session);
		buffer = ByteUtils.merge(buffer, (byte[]) data);
		if (buffer.length == 0) return;

		// #2: try to parse protocol
		ProtoParser parser = null;
		try {
			parser = ProtoParser.from(buffer);
		} catch (IllegalStateException e) {
			// Protocol is Not Ready!
			buffers.put(session, buffer);
			return;
		} catch (IllegalArgumentException e) {
			// Protocol Illegal!
			session.close();
			return;
		}

		// #3: let the others handle this!
		try {
			super.dataReceived(nextEntity, session, parser.result());
		} catch (Exception e) {
			throw e;	/* threr is nothing i can do! */
		} finally {
			this.dataReceived(nextEntity, session, ByteUtils.cut(parser.offset(), buffer));
		}
	}

	@Override
	public void sessionClosed(FilterEntity nextEntity, Session session)
			throws Exception {
		buffers.remove(session);
		super.sessionClosed(nextEntity, session);
	}
	
}