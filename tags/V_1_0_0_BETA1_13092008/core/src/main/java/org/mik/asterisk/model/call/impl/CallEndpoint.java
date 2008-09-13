package org.mik.asterisk.model.call.impl;

public class CallEndpoint {
	private String id;	
	
	public CallEndpoint(String id) {
		if(id == null) throw new IllegalArgumentException("id can not be null");
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
}