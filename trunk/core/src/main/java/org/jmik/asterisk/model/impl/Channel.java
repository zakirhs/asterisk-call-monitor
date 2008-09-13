package org.jmik.asterisk.model.impl;

import java.util.Date;

import org.apache.log4j.Logger;

/**
 * This channel is more or less equivalent to Connection in JTAPI model.
 * 
 * @author Michele La Porta
 * 
 */
public class Channel {

	private static Logger logger = Logger.getLogger(Channel.class);

	private Descriptor descriptor;
	private Call call;

	Channel(Descriptor descriptor, Call call) {
		if (descriptor == null){
			logger.error("descriptor is null");
			throw new AssertionError("descriptor can not be null");
		}
		if (call == null){
			logger.error("call is null");
			throw new AssertionError("call can not be null");
		}

		this.descriptor = descriptor;
		this.call = call;
	}

	public Channel(Descriptor descriptor) {
		this.descriptor = descriptor;
	}

	public Descriptor getDescriptor() {
		return this.descriptor;
	}

	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}

	public static class Descriptor {

		private String id;
		private Date creationTime;
		private CallEndpoint endpoint;

		public Descriptor(String id, Date creationTime, CallEndpoint endpoint) {
			if (id == null){
				logger.error("id is null");
				throw new IllegalArgumentException("id cannot be null");
			}
			if (creationTime == null){
				logger.error("creationTime is null");
				throw new IllegalArgumentException(
						"creationTime cannot be null");
			}
			if (endpoint == null){
				logger.error("endpoint is null");
				throw new IllegalArgumentException("endpoint cannot be null");
			}

			this.id = id;
			this.creationTime = creationTime;
			this.endpoint = endpoint;
		}

		public Descriptor(CallEndpoint endpoint) {
			if (endpoint == null)
				throw new IllegalArgumentException("endpoint cannot be null");
			this.endpoint = endpoint;
		}

		public String getId() {
			return id;
		}

		void setId(String id) {
			if (id == null){
				logger.error("id is null");
				throw new IllegalArgumentException("id cannot be null");
			}
						
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

	// @Override
	// public boolean equals(Object other) {
	// if ( (this == other ) ) return true;
	// if ( (other == null ) ) return false;
	// if ( !(other instanceof Channel) ) return false;
	// Channel castOther = ( Channel ) other;
	//        
	// return (this.getDescriptor().getChannel() ==
	// castOther.getDescriptor().getChannel());
	// /*&& (this.getDescriptor().getEndpoint().getCallId() ==
	// castOther.getDescriptor().getEndpoint().getCallId()
	// );*/
	// }
	//  
	// @Override
	// public int hashCode() {
	// int result = 17;
	// result = 37 * result +
	// (int)this.getDescriptor().getCreationTime().getTime();
	// return result;
	// }
//	 @Override
//	 public String toString() {
//	 return
//	 super.toString()+"[id="+this.getDescriptor().getId()+"]";
//	 }

}
