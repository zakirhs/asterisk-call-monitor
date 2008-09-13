package org.jmik.asterisk.model.impl;

import org.apache.log4j.Logger;

/**
 * This class represents a participant's address. This CallEndpoint is more or
 * less equivalent to Address in JTAPI model.
 * 
 * @author Michele La Porta
 * 
 */
public class CallEndpoint {

	private static Logger logger = Logger.getLogger(CallEndpoint.class);

	private String id;

	public CallEndpoint(String id) {
		if (id == null){
			logger.error("id is null");
			throw new IllegalArgumentException("id can not be null");
		}
		this.id = id;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return super.toString() + "[" + id + "]";
	}

}
