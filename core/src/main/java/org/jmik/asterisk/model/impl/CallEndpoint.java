package org.jmik.asterisk.model.impl;

/**
 * This class represents a participant's address.
 * This CallEndpoint is more or less equivalent to Address in JTAPI model.
 * @author Michele La Porta
 *
 */
public class CallEndpoint {

	private String callId;

	public CallEndpoint(String callId) {
		this.callId = callId;
	}
	
	public String getCallId() {
		return callId;
	}

	@Override
	public String toString() {
		return super.toString()+"["+callId+"]";
	}
	

}
