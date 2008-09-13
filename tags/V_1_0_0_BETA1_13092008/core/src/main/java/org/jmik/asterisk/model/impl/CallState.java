package org.jmik.asterisk.model.impl;

public enum CallState {

	IDLE(0, "IDLE"), 
	CONNECTING(1, "CONNECTING"), 
	ACTIVE(2, "ACTIVE"), 
	INVALID(3, "INVALID");

	private final int state;
	private final String description;

	CallState(int state, String description) {
		this.description = description;
		this.state = state;
	}

	public int state() {
		return state;
	}

	public String description() {
		return description;
	}

}
