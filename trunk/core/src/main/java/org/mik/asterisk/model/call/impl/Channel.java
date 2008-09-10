package org.mik.asterisk.model.call.impl;

import java.util.Date;

import org.mik.asterisk.model.call.Call;

public class Channel {
	private Descriptor descriptor;
	private Call call;
	
	Channel(Descriptor descriptor, Call call) {
		if(descriptor == null) throw new AssertionError("descriptor can not be null");
		if(call == null) throw new AssertionError("call can not be null");
		
		this.descriptor = descriptor;
		this.call = call;
	}
	
	public Descriptor getDescriptor() {
		return descriptor;
	}
	void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	public Call getCall() {
		return call;
	}
	void setCall(Call call) {
		this.call = call;
	}
	
	public static class Descriptor {
		private String id;
		private Date creationTime;
		private CallEndpoint endpoint;		
		
		public Descriptor(String id, Date creationTime, CallEndpoint endpoint) {
			if(id == null) throw new IllegalArgumentException("id can not be null");
			if(creationTime == null) throw new IllegalArgumentException("creationTime can not be null");
			if(endpoint == null) throw new IllegalArgumentException("endpoint can not be null");
			
			this.id = id;
			this.creationTime = creationTime;
			this.endpoint = endpoint;
		}		
		
		public Descriptor(CallEndpoint endpoint) {						
			if(endpoint == null) throw new IllegalArgumentException("endpoint can not be null");
			this.endpoint = endpoint;
		}	
		
		public String getId() {
			return id;
		}
		void setId(String id) {
			this.id = id;
		}
		
		public CallEndpoint getEndpoint() {
			return endpoint;
		}
		void setEndpoint(CallEndpoint endpoint) {
			this.endpoint = endpoint; 
		}		
		
		public Date getCreationTime() {
			return creationTime;
		}
		void setCreationTime(Date creationTime) {
			this.creationTime = creationTime;
		}
	}
}