package com.chinaroad.bubble.proto;

import com.chinaroad.bubble.proto.Protocol.Type;

class Header {
	
	private Type type;

	public Header(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}